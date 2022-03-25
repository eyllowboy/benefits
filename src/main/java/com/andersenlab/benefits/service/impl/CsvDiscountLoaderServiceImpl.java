package com.andersenlab.benefits.service.impl;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.CategoryRepository;
import com.andersenlab.benefits.repository.CompanyRepository;
import com.andersenlab.benefits.repository.CsvDiscountLoaderRepository;
import com.andersenlab.benefits.repository.DiscountRepository;
import com.andersenlab.benefits.service.CsvDiscountLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
	private String[] header;
	private final String id = "number";
	private final String companyTitle = "company_title";
	private final String type = "type";
	private final String category = "category";
	private final String image = "image";
	private final String companyDescription = "company_description";
	private final String companyAddress = "company_address";
	private final String companyPhone = "company_phone";
	private final String links = "links";
	private final String discountSize = "size";
	private final String discountType = "discount_type";
	private final String discountDescription = "discount_description";
	private final String discountCondition = "discount_condition";
	private final String startDate = "start_date";
	private final String endDate = "end_date";
	private final String location = "location";

	private final CsvDiscountLoaderRepository csvDiscountLoaderRepository;

	private final CategoryRepository categoryRepository;

	private final CompanyRepository companyRepository;

	private final DiscountRepository discountRepository;

	@Autowired
	public CsvDiscountLoaderServiceImpl(CsvDiscountLoaderRepository csvDiscountLoaderRepository,
										CategoryRepository categoryRepository,
										CompanyRepository companyRepository,
										DiscountRepository discountRepository) {
		this.csvDiscountLoaderRepository = csvDiscountLoaderRepository;
		this.categoryRepository = categoryRepository;
		this.companyRepository = companyRepository;
		this.discountRepository = discountRepository;
	}

	private Date getDate(String date, boolean isStartDate) {
		try {
			return (new SimpleDateFormat("dd.MM.yyyy")).parse(date);
		} catch (ParseException e) {
			return Date.from(isStartDate ?
				LocalDate.now().with(firstDayOfYear()).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant() :
				LocalDate.now().plus(100L, ChronoUnit.YEARS).with(lastDayOfYear()).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		}
	}

	private boolean isHeadersSuitable() {
		return (this.header[0].equals(id) &&
				this.header[1].equals(companyTitle) &&
				this.header[2].equals(type) &&
				this.header[3].equals(category) &&
				this.header[4].equals(image) &&
				this.header[5].equals(companyDescription) &&
				this.header[6].equals(companyAddress) &&
				this.header[7].equals(companyPhone) &&
				this.header[8].equals(links) &&
				this.header[9].equals(discountSize) &&
				this.header[10].equals(discountType) &&
				this.header[11].equals(discountDescription) &&
				this.header[12].equals(discountCondition) &&
				this.header[13].equals(startDate) &&
				this.header[14].equals(endDate) &&
				this.header[15].equals(location)
				);
	}

	private Map<String, String> parseLine(String[] splittedLine) {
		Map<String, String> result = new LinkedHashMap<>(this.header.length * 2);
		for (int i = 0; i < this.header.length; i++) {
			result.put(this.header[i], splittedLine[i]);
		}
		return result;
	}

	private List<String> splitMultilineValue(String value) {
		return (Arrays.asList(value.split("\\|")));
	}

	private CompanyEntity getCompany(Map<String, String> row) {
			return this.csvDiscountLoaderRepository.findCompanyByTitle(row.get(this.companyTitle)).orElse(
					new CompanyEntity(row.get(this.companyTitle),
							row.get(this.companyDescription),
							row.get(this.companyAddress),
							row.get(this.companyPhone),
							row.get(this.links)));
	}

	private Set<LocationEntity> getLocation(Map<String, String> row) {
		Set<LocationEntity> locations = new HashSet<>();
		splitMultilineValue(row.get(this.location))
			.forEach(city -> locations.add(this.csvDiscountLoaderRepository.findLocationByCity(city)
				.orElseThrow(() -> new IllegalStateException("City " + city + " was not found in database"))));
		return locations;
	}

	private Set<CategoryEntity> getCategory(Map<String, String> row) {
		Set<CategoryEntity> categories = new HashSet<>();
		splitMultilineValue(row.get(this.category))
			.forEach(title -> categories.add(this.categoryRepository.findByTitle(title)
				.orElseThrow(() -> new IllegalStateException("Category " + title + " was not found in database"))));
		return categories;
	}

	private DiscountEntity getDiscount(Map<String, String> row, CompanyEntity companyEntity) throws IllegalStateException {
		Set<LocationEntity> locations = getLocation(row);
		Set<CategoryEntity> categories = getCategory(row);
		DiscountEntity discountEntity = new DiscountEntity();
		discountEntity.setType(row.get(this.type));
		discountEntity.setDescription(row.get(this.discountDescription));
		discountEntity.setDiscount_condition(row.get(this.discountCondition));
		discountEntity.setSizeDiscount(row.get(this.discountSize));
		discountEntity.setDiscount_type(DiscountType.valueOf(row.get(this.discountType)));
		discountEntity.setDateBegin(getDate(row.get(this.startDate), true));
		discountEntity.setDateFinish(getDate(row.get(this.endDate), false));
		discountEntity.setImageDiscount(row.get(this.image));
		discountEntity.setArea(locations);
		discountEntity.setCategories(categories);
		discountEntity.setCompany_id(companyEntity);
		return discountEntity;
	}

	private void compareDiscounts(DiscountEntity discount1, DiscountEntity discount2) throws IllegalStateException {
		if ((discount1 == discount2) ||
			(discount1.getType().equals(discount2.getType()) &&
			discount1.getDescription().equals(discount2.getDescription()) &&
			discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
			discount1.getSizeDiscount().equals(discount2.getSizeDiscount()) &&
			discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
			discount1.getArea().equals(discount2.getArea()) &&
			discount1.getCategories().equals(discount2.getCategories()) &&
			discount1.getCompany_id().equals(discount2.getCompany_id())
			))
		throw new IllegalStateException("SKIP already exists");
	}

	@Transactional(rollbackFor = {DataIntegrityViolationException.class, IllegalStateException.class})
	private String putRowToTables (Map<String, String> row) {
		try {
			CompanyEntity companyEntity = getCompany(row);
			if (null == companyEntity.getId())
				this.companyRepository.save(companyEntity);
			List<DiscountEntity> discountEntities = this.csvDiscountLoaderRepository.findDiscountByCompanyId(companyEntity);
			DiscountEntity newDiscount = getDiscount(row, companyEntity);
			discountEntities.forEach(discount -> compareDiscounts(discount, newDiscount));
			this.discountRepository.save(newDiscount);
			return (row.get(this.id) + ": OK");
		} catch (IllegalStateException ex) {
			return (row.get(this.id) + ": " + ex.getMessage());
		} catch (DataIntegrityViolationException ex) {
			return (row.get(this.id) + ": " + ex.getCause().getCause());
		}
	}

	public List<String> loadDiscountsFromCsv(final MultipartFile file, String delimiter) {
		final List<String> response = new ArrayList<>();
		try {
			InputStreamReader isr = new InputStreamReader(file.getInputStream());
			try (BufferedReader input = new BufferedReader(isr)) {
				String line = input.readLine();
				this.header = line.split(delimiter);
				if (!isHeadersSuitable())
					throw new IllegalStateException("Headers titles not suitable");
				while (input.ready() && !line.isEmpty()) {
					line = input.readLine();
					String[] splittedLine = line.split(delimiter);
					response.add(splittedLine.length == this.header.length ?
							putRowToTables(parseLine(splittedLine)) :
							splittedLine[0] + ": Number of delimited fields does not match header");
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Check uploaded file is correct", ex);
		}
		return response;
	}
}
