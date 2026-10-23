package com.tfmsp.backend.entity;

import java.time.LocalDateTime;

import com.tfmsp.backend.enums.Role;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "bureau_id")
	private Long bureauId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(name = "full_name", nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true)
	private String phone;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified;

	@Column(name = "phone_verified", nullable = false)
	private boolean phoneVerified;

	@Column(name = "is_blocked", nullable = false)
	private boolean blocked;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private com.tfmsp.backend.enums.UserStatus status = com.tfmsp.backend.enums.UserStatus.PENDING;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "gender")
	private com.tfmsp.backend.enums.Gender gender;

	@Column(name = "date_of_birth")
	private java.time.LocalDate dateOfBirth;

	@Column(name = "last_active_at")
	private LocalDateTime lastActiveAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts = 0;

	@Column(name = "locked_until")
	private LocalDateTime lockedUntil;

	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBureauId() {
		return bureauId;
	}

	public void setBureauId(Long bureauId) {
		this.bureauId = bureauId;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public boolean isPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public boolean isBlocked() {
		return blocked || status == com.tfmsp.backend.enums.UserStatus.SUSPENDED || status == com.tfmsp.backend.enums.UserStatus.DELETED;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public com.tfmsp.backend.enums.UserStatus getStatus() {
		return status;
	}

	public void setStatus(com.tfmsp.backend.enums.UserStatus status) {
		this.status = status;
	}

	public LocalDateTime getLastActiveAt() {
		return lastActiveAt;
	}

	public void setLastActiveAt(LocalDateTime lastActiveAt) {
		this.lastActiveAt = lastActiveAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public LocalDateTime getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(LocalDateTime lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public boolean isAccountLocked() {
		return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
	}
	
	public com.tfmsp.backend.enums.Gender getGender() {
		return gender;
	}

	public void setGender(com.tfmsp.backend.enums.Gender gender) {
		this.gender = gender;
	}

	public java.time.LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(java.time.LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

}