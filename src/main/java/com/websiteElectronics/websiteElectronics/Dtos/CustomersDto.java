package com.websiteElectronics.websiteElectronics.Dtos;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomersDto {

    @CsvBindByPosition(position = 0)
    @CsvBindByName(column = "Customer Id")
    private int id;

    @CsvBindByPosition(position = 1)
    @CsvBindByName(column = "First Name")
    @NotNull(message = "First name is required")
    @Size(min = 3, message = "First name must be at least 2 characters")
    private String firstName;

    @CsvBindByPosition(position = 2)
    @CsvBindByName(column = "Last Name")
    @NotNull(message = "Last name is required")
    @Size(min = 3, message = "Last name must be at least 2 characters")
    private String lastName;

    @CsvBindByPosition(position = 3)
    @CsvBindByName(column = "Email")
    @NotNull(message = "Email is required")
    private String email;

    @CsvBindByPosition(position = 4)
    @CsvBindByName(column = "Password")
    @NotNull(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @CsvBindByPosition(position = 5)
    @CsvBindByName(column = "Phone Number")
    private String phoneNumber;

    @CsvBindByPosition(position = 6)
    @CsvBindByName(column = "Address")
    private String address;

    @CsvBindByPosition(position = 7)
    @CsvBindByName(column = "City")
    private String city;

    @CsvBindByPosition(position = 8)
    @CsvBindByName(column = "State")
    private String state;

    @CsvBindByPosition(position = 9)
    @CsvBindByName(column = "Zip Code")
    private String zipCore;

    @CsvBindByPosition(position = 10)
    @CsvBindByName(column = "Country")
    private String country;


}
