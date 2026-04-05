package com.group.book_selling.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders", indexes = {
		@Index(name = "idx_orders_order_number", columnList = "order_number", unique = true),
		@Index(name = "idx_orders_user_created_at", columnList = "user_id, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(name = "order_number", nullable = false, unique = true, length = 50)
	private String orderNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String customerEmail;

	@NotBlank
	@Column(nullable = false, length = 30)
	private String customerPhone;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String recipientName;

	@NotBlank
	@Column(nullable = false, length = 255)
	private String shippingStreetDetails;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String shippingWard;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String shippingDistrict;

	@NotBlank
	@Column(nullable = false, length = 120)
	private String shippingProvinceOrCity;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"), indexes = {
			@Index(name = "idx_order_items_sku", columnList = "sku")
	})
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();

	@Column(length = 50)
	private String couponCode;

	@Builder.Default
	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Builder.Default
	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal tax = BigDecimal.ZERO;

	@Builder.Default
	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discount = BigDecimal.ZERO;

	@Builder.Default
	@NotNull
	@DecimalMin(value = "0.0", inclusive = true)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal grandTotal = BigDecimal.ZERO;

	@Builder.Default
	@NotBlank
	@Column(nullable = false, length = 3)
	private String currency = "VND";

	@Builder.Default
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus orderStatus = OrderStatus.PENDING_PAYMENT;

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Payment payment;

	private LocalDateTime placedAt;

	@Column(updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.placedAt == null) {
			this.placedAt = now;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void attachPayment(Payment payment) {
		this.payment = payment;
		if (payment != null) {
			payment.setOrder(this);
		}
	}
}
