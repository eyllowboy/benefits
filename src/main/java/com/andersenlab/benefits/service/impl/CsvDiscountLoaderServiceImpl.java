package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.repository.LocationRepository;
import com.andersenlab.benefits.service.CsvDiscountLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

/***
 * Implementation for performing import CSV files
 * @author Denis Popov
 * @version 1.0
 * @see CsvDiscountLoaderService
 */
@Service
public class CsvDiscountLoaderServiceImpl implements CsvDiscountLoaderService {
	private final Map<String, String> suitableHeader = new LinkedHashMap<>(){{
		put("id", 					"number");
		put("companyTitle", 		"company_title");
		put("type", 				"type");
		put("category",				"category");
		put("image",				"image");
		put("companyDescription",	"company_description");
		put("companyAddress",		"company_address");
		put("companyPhone",			"company_phone");
		put("links",				"links");
		put("discountSize", 		"size");
		put("discountType", 		"discount_type");
		put("discountDescription",	"discount_description");
		put("discountCondition",	"discount_condition");
		put("startDate",			"start_date");
		put("endDate",				"end_date");
		put("location",				"location");
	}};
	private final List<String> header = new ArrayList<>();
	private final CategoryRepository categoryRepository;
	private final CompanyRepository companyRepository;
	private final DiscountRepository discountRepository;
	private final LocationRepository locationRepository;

	@Autowired
	public CsvDiscountLoaderServiceImpl(final CategoryRepository categoryRepository,
										final CompanyRepository companyRepository,
										final DiscountRepository discountRepository,
										final LocationRepository locationRepository) {
		this.categoryRepository = categoryRepository;
		this.companyRepository = companyRepository;
		this.discountRepository = discountRepository;
		this.locationRepository = locationRepository;
	}

	public List<String> loadDiscountsFromCsv(final MultipartFile file, final String delimiter) {
		final List<String> response = new ArrayList<>();
		try {
			final InputStreamReader isr = new InputStreamReader(file.getInputStream());
			try (BufferedReader input = new BufferedReader(isr)) {
				String line = input.readLine();
				this.header.clear();
				this.header.addAll(Arrays.stream(line.split(delimiter)).toList());
				checkHeadersSuitable();
				while (input.ready() && !line.isEmpty()) {
					line = input.readLine();
					final String[] splittedLine = line.split(delimiter);
					response.add(splittedLine.length == this.header.size() ?
							putRowToTables(parseLine(splittedLine)) :
							splittedLine[0] + ": Number of delimited fields does not match header");
				}
			}
		} catch (final IllegalStateException ex) {
			throw new IllegalStateException("Headers titles not suitable");
		} catch (final IOException ex) {
			throw new IllegalStateException("Check uploaded file is correct", ex);
		}
		return response;
	}

	@Transactional(rollbackFor = {DataIntegrityViolationException.class, IllegalStateException.class})
	public String putRowToTables (final Map<String, String> row) {
		try {
			final CompanyEntity companyEntity = getCompany(row);
			if (Objects.isNull(companyEntity.getId())) {
				validateCompany(companyEntity);
				companyEntity.setId(this.companyRepository.save(companyEntity).getId());
			}
			final long companyId = companyEntity.getId();
			final List<DiscountEntity> discounts = this.discountRepository.findAll();
			final DiscountEntity newDiscount = getDiscount(row, companyEntity);
			discounts.stream().filter(discount ->
								!Objects.isNull(discount.getCompany().getId()) &&
									discount.getCompany().getId().equals(companyId) &&
									equalDiscounts(discount, newDiscount))
					.findFirst().ifPresent(found -> {throw new IllegalStateException("SKIP already exists");});
			validateDiscount(newDiscount);
			this.discountRepository.save(newDiscount);
			return (row.get(this.suitableHeader.get("id")) + ": OK");
		} catch (final IllegalStateException ex) {
			return (row.get(this.suitableHeader.get("id")) + ": " + ex.getMessage());
		} catch (final DataIntegrityViolationException ex) {
			return (row.get(this.suitableHeader.get("id")) + ": " + ex.getCause().getCause());
		}
	}

	private Date getDate(final String date, final boolean isStartDate) {
		try {
			return (new SimpleDateFormat("dd.MM.yyyy")).parse(date);
		} catch (final ParseException e) {
			return Date.from(isStartDate ?
					LocalDate.now().with(firstDayOfYear()).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant() :
					LocalDate.now().plus(100L, ChronoUnit.YEARS).with(lastDayOfYear()).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		}
	}

	private void checkHeadersSuitable() throws IOException, IllegalStateException {
		if (this.header.size() == 0)
			throw new IOException();
		final Iterator<String> headerIterator = this.header.iterator();
		this.suitableHeader.forEach((key, value) -> {
			if (!headerIterator.next().equals(value))
				throw new IllegalStateException();
		});
	}

	private Map<String, String> parseLine(final String[] splittedLine) {
		final Map<String, String> result = new LinkedHashMap<>(this.header.size() * 2);
		final Iterator<String> headerTitle = this.suitableHeader.values().iterator();
		Arrays.stream(splittedLine).forEach(item -> result.put(headerTitle.next(), item));
		return result;
	}

	private List<String> splitMultilineValue(final String value) {
		return (Arrays.asList(value.split("\\|")));
	}

	private CompanyEntity getCompany(final Map<String, String> row) {
		return this.companyRepository.findAll().stream().filter(company ->
						company.getTitle().equals(row.get(this.suitableHeader.get("companyTitle")))).findFirst()
				.orElse(new CompanyEntity(
						row.get(this.suitableHeader.get("companyTitle")),
						row.get(this.suitableHeader.get("companyDescription")),
						row.get(this.suitableHeader.get("companyAddress")),
						row.get(this.suitableHeader.get("companyPhone")),
						row.get(this.suitableHeader.get("links"))));
	}

	private Set<LocationEntity> getLocation(final Map<String, String> row) throws IllegalStateException {
		final List<LocationEntity> locations = this.locationRepository.findAll();
		final List<String> searchedCities = splitMultilineValue(row.get(this.suitableHeader.get("location")));
		final Set<LocationEntity> result  = new LinkedHashSet<>();
		searchedCities.forEach(city -> result.add(locations.stream().filter(location ->
				location.getCity().equals(city)).findFirst().orElseThrow(() ->
				new IllegalStateException("City " + city + " was not found in database"))));
		return result;
	}

	private Set<CategoryEntity> getCategory(final Map<String, String> row) {
		final List<CategoryEntity> categories = this.categoryRepository.findAll();
		final List<String> searchedCategories = splitMultilineValue(row.get(this.suitableHeader.get("category")));
		final Set<CategoryEntity> result = new LinkedHashSet<>();
		searchedCategories.forEach(title -> result.add(categories.stream().filter(category ->
				category.getTitle().equals(title)).findFirst().orElseThrow(() ->
				new IllegalStateException("Category " + title + " was not found in database"))));
		return result;
	}

	private DiscountEntity getDiscount(final Map<String, String> row, final CompanyEntity companyEntity) throws IllegalStateException {
		final Set<LocationEntity> locations = getLocation(row);
		final Set<CategoryEntity> categories = getCategory(row);
		final DiscountEntity discountEntity = new DiscountEntity();
		discountEntity.setType(row.get(this.suitableHeader.get("type")));
		discountEntity.setDescription(row.get(this.suitableHeader.get("discountDescription")));
		discountEntity.setDiscount_condition(row.get(this.suitableHeader.get("discountCondition")));
		discountEntity.setSizeDiscount(row.get(this.suitableHeader.get("discountSize")));
		discountEntity.setDiscount_type(DiscountType.valueOf(row.get(this.suitableHeader.get("discountType"))));
		discountEntity.setDateBegin(getDate(row.get(this.suitableHeader.get("startDate")), true));
		discountEntity.setDateFinish(getDate(row.get(this.suitableHeader.get("endDate")), false));
		discountEntity.setImageDiscount(row.get(this.suitableHeader.get("image")));
		discountEntity.setArea(locations);
		discountEntity.setCategories(categories);
		discountEntity.setCompany(companyEntity);
		return discountEntity;
	}

	private boolean equalCompanies(final CompanyEntity company1, final CompanyEntity company2) {
		return (company1.getTitle().equals(company2.getTitle()) &&
				company1.getAddress().equals(company2.getAddress()) &&
				company1.getDescription().equals(company2.getDescription()) &&
				company1.getPhone().equals(company2.getPhone()) &&
				company1.getLink().equals(company2.getLink())
		);
	}

	private boolean equalDiscounts(final DiscountEntity discount1, final DiscountEntity discount2) throws IllegalStateException {
		return ((discount1 == discount2) ||
				(discount1.getType().equals(discount2.getType()) &&
						discount1.getDescription().equals(discount2.getDescription()) &&
						discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
						discount1.getSizeDiscount().equals(discount2.getSizeDiscount()) &&
						discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
						equalCompanies(discount1.getCompany(), discount2.getCompany())
				));
	}

	public void validateDiscount(@Valid final DiscountEntity discount) {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			final Validator validator = validatorFactory.usingContext().getValidator();
			final Set<ConstraintViolation<DiscountEntity>> constrains = validator.validate(discount);
			if (constrains.size() > 0)
				throw new IllegalStateException(constrains.iterator().next().getMessage());
		}
	}

	public void validateCompany(@Valid final CompanyEntity company) {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			final Validator validator = validatorFactory.usingContext().getValidator();
			final Set<ConstraintViolation<CompanyEntity>> constrains = validator.validate(company);
			if (constrains.size() > 0)
				throw new IllegalStateException(constrains.iterator().next().getMessage());
		}
	}

}
