package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.Role;

import jakarta.validation.constraints.*;

public class RegisterRequest {
	
	@NotBlank(message = "Name cannot be blank")
	@Size(max = 100, message = "Name is too long")
	private String fullName;
	
	@Email(message = "Email must be in valid format")
	@NotBlank(message = "Email cannot be blank")
	private String email;
	
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be 10 digits")
	@NotBlank(message = "Phone number cannot be blank")
	private String phone;
	
	@Size(min = 8, max = 50, message = "Password must be 8 to 50 characters")
	@NotBlank(message = "Password cannot be blank")
	private String password;
	
	@NotNull(message = "Role cannot be null")
	private Role role;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "RegisterRequest [fullName=" + fullName + ", email=" + email + ", phone=" + phone + ", role=" + role
				+ "]";
	}
	
}
