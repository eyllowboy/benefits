package com.andersenlab.benefits.domain;



import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(name = "discounts")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id", nullable=false)
    private Integer id;

    @Column(name="company_id")
    private Integer CategoryId;

    @Column(name="company_id")
    private Integer CompanyId;

    @Column(name="title", nullable=false)
    @Size(max = 50)
    private String title;

    @Column(name="description", nullable=false)
    @Size(max = 2000)
    private String description;

    @Column(name="size", nullable=false)
    @Size(max = 100)
    private Byte sizeDiscount;

    @Column(name="start_date", nullable=false)
    @Temporal(TemporalType.DATE)
    private LocalDate dateBegin;

    @Column(name="end_date")
    @Temporal(TemporalType.DATE)
    private LocalDate dateFinish;

    @Column(name="area", nullable=false)
    private Integer area;

    @Column(name="image", nullable=false)
    @Size(max = 300)
    private String imageDiscount;

    public Discount() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        CategoryId = categoryId;
    }

    public Integer getCompanyId() {
        return CompanyId;
    }

    public void setCompanyId(Integer companyId) {
        CompanyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Byte getSizeDiscount() {
        return sizeDiscount;
    }

    public void setSizeDiscount(Byte sizeDiscount) {
        this.sizeDiscount = sizeDiscount;
    }

    public LocalDate getDateBegin() {
        return dateBegin;
    }

    public void setDateBegin(LocalDate dateBegin) {
        this.dateBegin = dateBegin;
    }

    public LocalDate getDateFinish() {
        return dateFinish;
    }

    public void setDateFinish(LocalDate dateFinish) {
        this.dateFinish = dateFinish;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public String getImageDiscount() {
        return imageDiscount;
    }

    public void setImageDiscount(String imageDiscount) {
        this.imageDiscount = imageDiscount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount discount = (Discount) o;
        return Objects.equals(id, discount.id) &&
                Objects.equals(CategoryId, discount.CategoryId) &&
                Objects.equals(CompanyId, discount.CompanyId) &&
                Objects.equals(title, discount.title) &&
                Objects.equals(description, discount.description) &&
                Objects.equals(sizeDiscount, discount.sizeDiscount) &&
                Objects.equals(dateBegin, discount.dateBegin) &&
                Objects.equals(dateFinish, discount.dateFinish) &&
                Objects.equals(area, discount.area) &&
                Objects.equals(imageDiscount, discount.imageDiscount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, CategoryId, CompanyId, title, description, sizeDiscount, dateBegin, dateFinish, area, imageDiscount);
    }

    @Override
    public String toString() {
        return "Discount{" +
                "id=" + id +
                ", CategoryId=" + CategoryId +
                ", CompanyId=" + CompanyId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", sizeDiscount=" + sizeDiscount +
                ", dateBegin=" + dateBegin +
                ", dateFinish=" + dateFinish +
                ", area=" + area +
                ", imageName='" + imageDiscount + '\'' +
                '}';
    }
}
