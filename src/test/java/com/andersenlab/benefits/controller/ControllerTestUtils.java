package com.andersenlab.benefits.controller;

import com.andersenlab.benefits.domain.*;
import com.andersenlab.benefits.repository.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.sql.Timestamp.valueOf;

@Component
public class ControllerTestUtils {
    @Autowired
    private final DiscountRepository discountRepository;
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final int listLength = 10;

    @Autowired
    public ControllerTestUtils(final RoleRepository roleRepository,
                               final DiscountRepository discountRepository,
                               final CategoryRepository categoryRepository,
                               final LocationRepository locationRepository,
                               final CompanyRepository companyRepository,
                               final UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.discountRepository = discountRepository;
        this.categoryRepository = categoryRepository;
        this.companyRepository = companyRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    public void clearTables() {
        this.discountRepository.deleteAll();
        this.categoryRepository.deleteAll();
        this.companyRepository.deleteAll();
        this.userRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.roleRepository.deleteAll();
    }

    public int getRndEntityPos() {
        return (int) (random() * (listLength - 1) + 1);
    }

    public CompanyEntity getCompany(final long num) {
        return (this.companyRepository.findAll().stream().filter(item ->
                    Objects.equals(item.getTitle(), "Company" + num)).findFirst()
                .orElse(
                    this.companyRepository.save(new CompanyEntity(
                    "Company" + num,
                    "Description" + num,
                    "Address" + num,
                    "Phone" + num,
                    "Link" + num)
        )));
    }

    public Set<CategoryEntity> getCategoryList() {
        Set<CategoryEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * (this.listLength - 1)  + 1);
        for (long i = 1; i <= size; i++) {
            CategoryEntity category = this.categoryRepository.findByTitle("Category" + i)
                    .orElse(new CategoryEntity("Category" + i));
            if (null == category.getId())
                this.categoryRepository.save(category);
            result.add(category);
        }
        return result;
    }

    public LocationEntity getLocation(final long num) {
        final LocationEntity location = this.locationRepository.findAll().stream()
                .filter(item -> item.getCity().equals("someCity" + num)).findFirst()
                .orElse(new LocationEntity("SomeCountry", "someCity" + num));
        if (null == location.getId())
            this.locationRepository.save(location);
        return location;
    }

    public Set<LocationEntity> getLocationList() {
        Set<LocationEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * (this.listLength - 1)  + 1);
        for (long i = 1; i <= size; i++)
            result.add(getLocation(i));
        return result;
    }

    public RoleEntity getRole(final long num) {
        return this.roleRepository.findByCode("code" + num)
                .orElse(new RoleEntity("roleName" + num, "code" + num));
    }

    public List<RoleEntity> getRoleList() {
        List<RoleEntity> result = new ArrayList<>(this.listLength);
        for (long i = 1; i <= this.listLength; i++)
            result.add(getRole(i));
        return result;
    }

    public UserEntity getUser(final long num) {
        return (this.userRepository.findByLogin("userLogin" + num).orElseGet(() -> {
            UserEntity newUser = new UserEntity("userLogin" + num, getRole(num), getLocation(num));
            if (null == newUser.getRoleEntity().getId())
                this.roleRepository.save(newUser.getRoleEntity());
            return newUser;
        }));
    }

    public List<UserEntity> getUserList() {
        List<UserEntity> result = new ArrayList<>(this.listLength);
        for (long i = 1; i <= this.listLength; i++)
            result.add(getUser(i));
        return result;
    }

    public DiscountEntity getDiscount(final long num) {
        return (this.discountRepository.findAll().stream().filter(item ->
                        Objects.equals(item.getId(), num)).findFirst()
                .orElse(
                        new DiscountEntity(
                                num,
                                "Type" + num,
                                "Description" + num,
                                "Condition" + num,
                                "Size" + num,
                                DiscountType.DISCOUNT,
                                valueOf("2022-01-01 00:00:00"),
                                valueOf("2022-12-31 00:00:00"),
                                "Image" + num,
                                getLocationList(),
                                getCategoryList(),
                                getCompany(num))));
    }

    public List<DiscountEntity> getDiscountList() {
        List<DiscountEntity> result = new ArrayList<>();
        for (long i = 1; i <= this.listLength; i++) {
            result.add(getDiscount(i));
        }
        return result;
    }

    public String discountToString(final DiscountEntity discount) {
        return (
                discount.getId() + ";" +
                        discount.getCompany_id().getTitle() + ";" +
                        discount.getType() + ";" +
                        discount.getCategories().stream().map(CategoryEntity::getTitle).collect(Collectors.joining("|")) + ";" +
                        discount.getImageDiscount() + ";" +
                        discount.getCompany_id().getDescription() + ";" +
                        discount.getCompany_id().getAddress() + ";" +
                        discount.getCompany_id().getPhone() + ";" +
                        discount.getCompany_id().getLink() + ";" +
                        discount.getSizeDiscount() + ";" +
                        discount.getDiscount_type() + ";" +
                        discount.getDescription() + ";" +
                        discount.getDiscount_condition() + ";" +
                        discount.getDateBegin() + ";" +
                        discount.getDateFinish() + ";" +
                        discount.getArea().stream().map(LocationEntity::getCity).collect(Collectors.joining("|"))
        );
    }

    public MockMultipartFile newMockMultipartFile(final List<DiscountEntity> discounts) {
        StringBuilder contents = new StringBuilder("number;company_title;type;category;image;company_description;company_address;company_phone;links;size;discount_type;discount_description;discount_condition;start_date;end_date;location");
        discounts.forEach(discount -> contents.append("\n").append(discountToString(discount)));
        return (new MockMultipartFile(
                "file",
                "discounts.csv",
                "multipart/form-data",
                contents.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public boolean isCompaniesEquals(final CompanyEntity company1, final CompanyEntity company2) {
        return (
                company1.getTitle().equals(company2.getTitle()) &&
                        company1.getAddress().equals(company2.getAddress()) &&
                        company1.getDescription().equals(company2.getDescription()) &&
                        company1.getPhone().equals(company2.getPhone()) &&
                        company1.getLink().equals(company2.getLink())
        );
    }

    public boolean isDiscountsEquals(final DiscountEntity discount1, final DiscountEntity discount2) {
        if (discount1 == discount2) return true;
        if (null == discount1 || discount1.getClass() != discount2.getClass()) return false;
        return (
                discount1.getType().equals(discount2.getType()) &&
                        discount1.getDescription().equals(discount2.getDescription()) &&
                        discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
                        discount1.getSizeDiscount().equals(discount2.getSizeDiscount()) &&
                        discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
                        isCompaniesEquals(discount1.getCompany_id(), discount2.getCompany_id())
        );
    }

    public RoleEntity getRoleFromJson(final JSONObject role) throws JSONException {
        Long roleId = Long.parseLong(role.getString("id"));
        String roleName = role.getString("name");
        String roleCode = role.getString("code");
        return this.roleRepository.findById(roleId)
                .orElse(new RoleEntity(roleId, roleName, roleCode));
    }

    public List<RoleEntity> getRolesFromJson(final String json) throws JSONException {
        JSONArray jsonObjects = new JSONArray(json);
        List<RoleEntity> result = new ArrayList<>(jsonObjects.length());
        for (int i = 0; i < jsonObjects.length(); i++)
            result.add(getRoleFromJson(jsonObjects.getJSONObject(i)));
        return result;
    }

    public LocationEntity getLocationFromJson(final JSONObject location) throws JSONException {
        Long locationId = Long.parseLong(location.getString("id"));
        String locationCountry = location.getString("country");
        String locationCity = location.getString("city");
        return this.locationRepository.findById(locationId)
                .orElse(new LocationEntity(locationId, locationCountry, locationCity));
    }

    public Set<LocationEntity> getLocationsFromJson(final JSONObject json) throws JSONException {
        JSONArray jsonObjects = new JSONArray(json);
        Set<LocationEntity> result = new LinkedHashSet<>(jsonObjects.length());
        for (int i = 0; i < jsonObjects.length(); i++)
            result.add(getLocationFromJson(jsonObjects.getJSONObject(i)));
        return result;
    }

    public UserEntity getUserFromJson(final JSONObject user) throws JSONException {
        Long userId = Long.parseLong(user.getString("id"));
        return this.userRepository.findById(userId).orElseGet(() -> {
            try {
                String userLogin = user.getString("login");
                return (new UserEntity(
                        userLogin,
                        getRoleFromJson(user.getJSONObject("roleEntity")),
                        getLocationFromJson(user.getJSONObject("location"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public List<UserEntity> getUsersFromJson(final String json) throws JSONException {
        JSONArray jsonObjects = new JSONArray(json);
        List<UserEntity> result = new ArrayList<>(jsonObjects.length());
        for (int i = 0; i < jsonObjects.length(); i++)
            result.add(getUserFromJson(jsonObjects.getJSONObject(i)));
        return result;
    }

    public CategoryEntity getCategoryFromJson(final JSONObject category) throws JSONException {
        Long categoryId = Long.parseLong(category.getString("id"));
        return this.categoryRepository.findById(categoryId).orElseGet(() -> {
            try {
                return (new CategoryEntity(
                        categoryId,
                        category.getString("title"),
                        null
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public Set<CategoryEntity> getCategoriesFromJson(final JSONObject json) throws JSONException {
        JSONArray jsonObjects = new JSONArray(json);
        Set<CategoryEntity> result = new LinkedHashSet<>(jsonObjects.length());
        for (int i = 0; i < jsonObjects.length(); i++) {
            result.add(getCategoryFromJson(jsonObjects.getJSONObject(i)));
        }
        return result;
    }

    public CompanyEntity getCompanyFromJson(final JSONObject company) throws JSONException {
        Long companyId = Long.parseLong(company.getString("id"));
        return this.companyRepository.findById(companyId).orElseGet(() -> {
            try {
                return (new CompanyEntity(
                        companyId,
                        company.getString("title"),
                        company.getString("description"),
                        company.getString("address"),
                        company.getString("phone"),
                        company.getString("link")
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public DiscountEntity getDiscountFromJson(final JSONObject discount) throws JSONException {
        Long discountId = Long.parseLong(discount.getString("id"));
        return this.discountRepository.findById(discountId).orElseGet(() -> {
            try {
                return (new DiscountEntity(
                        discountId,
                        discount.getString("type"),
                        discount.getString("description"),
                        discount.getString("discount_condition"),
                        discount.getString("sizeDiscount"),
                        DiscountType.valueOf(discount.getString("discount_type")),
                        valueOf(discount.getString("dateBegin")),
                        valueOf(discount.getString("dateFinish")),
                        discount.getString("imageDiscount"),
                        getLocationsFromJson(discount.getJSONObject("area")),
                        getCategoriesFromJson(discount.getJSONObject("categories")),
                        getCompanyFromJson(discount.getJSONObject("company_id")
                        )));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public List<DiscountEntity> getDiscountsFromJson(final String json) throws JSONException {
        JSONArray jsonObjects = new JSONArray(json);
        List<DiscountEntity> result = new ArrayList<>(jsonObjects.length());
        for (int i = 0; i < jsonObjects.length(); i++) {
            result.add(getDiscountFromJson(jsonObjects.getJSONObject(i)));
        }
        return result;
    }
}
