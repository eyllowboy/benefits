package com.andersenlab.benefits.service;

import com.andersenlab.benefits.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.Math.random;
import static java.sql.Timestamp.valueOf;

@Component
public class ServiceTestUtils {

    private static final int listLength = 10;

    public static int getRndEntityPos() {
        return (int) (random() * (listLength - 1) + 1);
    }

    public static CategoryEntity getCategory(final long num) {
        return new CategoryEntity("Category" + num);
    }

    public static Set<CategoryEntity> getCategoryList() {
        final Set<CategoryEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * 9 + 1);
        for (long i = 1; i <= size; i++) {
            result.add(getCategory(i));
        }
        return result;
    }

    public static LocationEntity getLocation(final long num) {
        return new LocationEntity("SomeCountry", "City" + num);
    }

    public static Set<LocationEntity> getLocationList() {
        final Set<LocationEntity> result = new LinkedHashSet<>();
        final int size = (int) (random() * 9 + 1);
        for (long i = 1; i <= size; i++) {
            result.add(getLocation(i));
        }
        return result;
    }

    public static List<LocationEntity> getLocationList(final int size) {
        final List<LocationEntity> result = new ArrayList<>();
        for (long i = 1; i <= size; i++) {
            result.add(getLocation(i));
        }
        return result;
    }

    public static CompanyEntity getCompany(final long num) {
        return (new CompanyEntity(
                "Company" + num,
                "Description" + num,
                "Address" + num,
                "Phone" + num,
                "Link" + num
        ));
    }

    public static List<CompanyEntity> getCompanyList() {
        final List<CompanyEntity> result = new ArrayList<>(listLength);
        for (int i = 1; i <= listLength; i++) {
            result.add(getCompany(i));
        }
        return result;
    }

    public static RoleEntity getRole(final long num) {
        final RoleEntity role = new RoleEntity("roleName" + num, "code" + num);
        role.setId(num);
        return role;
    }

    public static List<RoleEntity> getRoleList() {
        final List<RoleEntity> result = new ArrayList<>(listLength);
        for (int i = 1; i <= listLength; i++) {
            result.add(getRole(i));
        }
        return result;
    }

    public static UserEntity getUser(final long num) {
        final UserEntity user = new UserEntity("userLogin" + num, getRole(num), getLocation(num));
        user.setId(num);
        return user;
    }

    public static List<UserEntity> getUserList() {
        final List<UserEntity> result = new ArrayList<>(listLength);
        for (int i = 1; i <= listLength; i++) {
            result.add(getUser(i));
        }
        return result;
    }

    public static DiscountEntity getDiscount(final long num) {
        final int sizeA = (int) (random() * 99 + 1);
        final int sizeB = (int) (random() * 99 + 1);
        return new DiscountEntity(
                num,
                "Type" + num,
                "Description" + num,
                "Condition" + num,
                Math.min(sizeA, sizeB),
                Math.max(sizeA, sizeB),
                DiscountType.DISCOUNT,
                valueOf("2022-01-01 00:00:00"),
                valueOf("2022-12-31 00:00:00"),
                "Image" + num,
                getLocationList(),
                getCategoryList(),
                getCompany(getRndEntityPos()));
    }

    public static List<DiscountEntity> getDiscountList() {
        final List<DiscountEntity> result = new ArrayList<>();
        for (long i = 1; i <= listLength; i++) {
            result.add(getDiscount(i));
        }
        return result;
    }

    public static String discountToString(final DiscountEntity discount) {
        return (discount.getId() + ";" +
                discount.getCompany().getTitle() + ";" +
                discount.getType() + ";" +
                discount.getCategories().stream()
                        .map(CategoryEntity::getTitle).collect(Collectors.joining("|")) +
                ";" +
                discount.getImageDiscount() + ";" +
                discount.getCompany().getDescription() + ";" +
                discount.getCompany().getAddress() + ";" +
                discount.getCompany().getPhone() + ";" +
                discount.getCompany().getLink() + ";" +
                discount.getSizeMin() + ";" +
                discount.getSizeMax() + ";" +
                discount.getDiscount_type() + ";" +
                discount.getDescription() + ";" +
                discount.getDiscount_condition() + ";" +
                discount.getDateBegin() + ";" +
                discount.getDateFinish() + ";" +
                discount.getArea().stream()
                        .map(LocationEntity::getCity).collect(Collectors.joining("|")));
    }

    public static MockMultipartFile newMockMultipartFile(final List<DiscountEntity> discounts) {
        final StringBuilder contents = new StringBuilder("number;company_title;type;category;image;company_description;company_address;company_phone;links;min_sizeDiscount;max_sizeDiscount;discount_type;discount_description;discount_condition;start_date;end_date;location");
        discounts.forEach(discount -> contents.append("\n").append(discountToString(discount)));
        return (new MockMultipartFile(
                "file",
                "discounts.csv",
                "multipart/form-data",
                contents.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static boolean isDiscountsEquals(final DiscountEntity discount1, final DiscountEntity discount2) {
        if (discount1 == discount2) {
            return true;
        }
        if (Objects.isNull(discount1) || discount1.getClass() != discount2.getClass()) {
            return false;
        }
        return (
                discount1.getType().equals(discount2.getType()) &&
                        discount1.getDescription().equals(discount2.getDescription()) &&
                        discount1.getDiscount_condition().equals(discount2.getDiscount_condition()) &&
                        discount1.getSizeMin().equals(discount2.getSizeMin()) &&
                        discount1.getSizeMax().equals(discount2.getSizeMax()) &&
                        discount1.getImageDiscount().equals(discount2.getImageDiscount()) &&
                        isCompaniesEquals(discount1.getCompany(), discount2.getCompany())
        );
    }

    public static boolean isCompaniesEquals(final CompanyEntity company1, final CompanyEntity company2) {
        return (
                company1.getTitle().equals(company2.getTitle()) &&
                        company1.getAddress().equals(company2.getAddress()) &&
                        company1.getDescription().equals(company2.getDescription()) &&
                        company1.getPhone().equals(company2.getPhone()) &&
                        company1.getLink().equals(company2.getLink())
        );
    }

    public static <E> E saveItem(final Collection<E> collection, final E item, final BiFunction<E, E, Boolean> compareMethod) {
        final E result = collection.stream().filter(element -> compareMethod.apply(item, element)).findFirst().orElse(item);
        if (result == item) {
            collection.add(item);
            try {
                final Method setId = Arrays.stream(item.getClass().getMethods()).filter(method ->
                        Objects.equals(method.getName(), "setId")).findFirst().orElse(null);
                if (!Objects.isNull(setId))
                    setId.invoke(item, (long) collection.size());
            } catch (InvocationTargetException | IllegalAccessException ex) {
                return result;
            }
        }
        return result;
    }
}
