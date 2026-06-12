# 

## Quick Start Guide

### 1. Prerequisites
- Java 21 installed
- MySQL 8.0+ installed and running
- Maven 3.6+ installed

### 2. Database Setup

Login to MySQL and create the database:

```sql
CREATE DATABASE gvk_healthhub;
USE gvk_healthhub;

-- Create doctors table
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(200) NOT NULL UNIQUE,
    title VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    qualification VARCHAR(500),
    specialization VARCHAR(100),
    experience_years INT,
    designation VARCHAR(100),
    biography TEXT,
    profile_image_url VARCHAR(500),
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    consultation_fee_walkin DECIMAL(10,2),
    consultation_fee_video DECIMAL(10,2),
    allows_video_consultation BOOLEAN DEFAULT FALSE,
    allows_phone_consultation BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slug (slug),
    INDEX idx_specialization (specialization),
    INDEX idx_active (is_active)
);

-- Insert sample data
INSERT INTO doctors (slug, title, first_name, last_name, qualification, specialization, experience_years, designation, biography, profile_image_url, avg_rating, total_reviews, consultation_fee_walkin, consultation_fee_video, allows_video_consultation, allows_phone_consultation, is_active, is_featured) VALUES
('dr-ng-sastry', 'Dr.', 'N G', 'Sastry', 'MBBS, MD (General Medicine), DM (Endocrinology)', 'Diabetologist', 34, 'Senior Consultant & Medical Director', 'Dr. N G Sastry is a renowned diabetologist with over 34 years of experience in treating diabetes. He is the Medical Director of GVK Health Hub and has helped thousands of patients manage their diabetes effectively.', 'https://www.gvkhealthhub.com/images/doctors/dr-ng-sastry.jpg', 4.9, 890, 800, 600, TRUE, TRUE, TRUE, TRUE),
('dr-venu-goud', 'Dr.', 'Venu', 'Goud', 'MBBS, MD (General Medicine), DNB (Endocrinology)', 'Diabetologist', 15, 'Consultant Diabetologist', 'Dr. Venu Goud is an experienced diabetologist specializing in insulin pump therapy and continuous glucose monitoring. He has a patient-centric approach and ensures personalized treatment plans.', 'https://www.gvkhealthhub.com/images/doctors/dr-venu-goud.jpg', 4.8, 456, 700, 500, TRUE, FALSE, TRUE, FALSE);
```

### 3. Update Application Properties

Edit `src/main/resources/application.properties`:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/gvk_healthhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# Server Configuration
server.port=8080
server.servlet.context-path=/api/v1
```

### 4. Build and Run

```bash
# Navigate to project directory
cd gvk-healthhub-be

# Build the project (this will download dependencies)
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

### 5. Test the API

Once the application starts, test the following endpoints:

```bash
# Get all active doctors
curl http://localhost:8080/api/v1/doctors

# Get doctors by specialization
curl http://localhost:8080/api/v1/doctors?specialization=diabetologist

# Get featured doctors
curl http://localhost:8080/api/v1/doctors/featured

# Get doctor by slug
curl http://localhost:8080/api/v1/doctors/slug/dr-ng-sastry

# Search doctors
curl "http://localhost:8080/api/v1/doctors/search?keyword=sastry&specialization=diabetologist"
```

### 6. Access Swagger UI

Open your browser and go to:
```
http://localhost:8080/api/v1/swagger-ui.html
```

## API Endpoints

### 1. Get All Doctors
```
GET /api/v1/doctors
```

Response:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "slug": "dr-ng-sastry",
      "title": "Dr.",
      "firstName": "N G",
      "lastName": "Sastry",
      "fullName": "Dr. N G Sastry",
      "qualification": "MBBS, MD (General Medicine), DM (Endocrinology)",
      "specialization": "Diabetologist",
      "experienceYears": 34,
      "designation": "Senior Consultant & Medical Director",
      "biography": "Dr. N G Sastry is a renowned diabetologist...",
      "profileImageUrl": "https://...",
      "avgRating": 4.90,
      "totalReviews": 890,
      "consultationFeeWalkin": 800.00,
      "consultationFeeVideo": 600.00,
      "allowsVideoConsultation": true,
      "allowsPhoneConsultation": true,
      "isActive": true,
      "isFeatured": true
    }
  ],
  "total": 2,
  "page": 0,
  "size": 10,
  "totalPages": 1
}
```

### 2. Get Doctors by Specialization
```
GET /api/v1/doctors?specialization=diabetologist
```

### 3. Get Featured Doctors
```
GET /api/v1/doctors/featured
```

### 4. Get Doctor by Slug
```
GET /api/v1/doctors/slug/{slug}
```

### 5. Search Doctors
```
GET /api/v1/doctors/search?keyword=sastry&specialization=diabetologist&minExperience=10
```

## Project Structure

```
gvk-healthhub-be/
├── src/main/java/com/gvk/healthhub/
│   ├── GvkHealthhubBeApplication.java    # Main application class
│   ├── entity/
│   │   └── Doctor.java                   # Doctor entity
│   ├── repository/
│   │   └── DoctorRepository.java         # Data access layer
│   ├── service/
│   │   ├── DoctorService.java            # Service interface
│   │   └── DoctorServiceImpl.java        # Service implementation
│   ├── controller/
│   │   └── DoctorController.java         # REST API endpoints
│   ├── dto/
│   │   ├── DoctorDTO.java                # Data transfer object
│   │   └── ApiResponse.java              # Standard API response
│   └── exception/
│       └── GlobalExceptionHandler.java   # Error handling
├── src/main/resources/
│   ├── application.properties            # Configuration
│   └── data.sql                         # Sample data (optional)
└── pom.xml                              # Maven dependencies
```

## Database Table Structure

```sql
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slug VARCHAR(200) NOT NULL UNIQUE,
    title VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    qualification VARCHAR(500),
    specialization VARCHAR(100),
    experience_years INT,
    designation VARCHAR(100),
    biography TEXT,
    profile_image_url VARCHAR(500),
    avg_rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    consultation_fee_walkin DECIMAL(10,2),
    consultation_fee_video DECIMAL(10,2),
    allows_video_consultation BOOLEAN DEFAULT FALSE,
    allows_phone_consultation BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_slug (slug),
    INDEX idx_specialization (specialization),
    INDEX idx_active (is_active)
);
```

## Troubleshooting

### Issue: Port already in use
```bash
# Change port in application.properties
server.port=8081
```

### Issue: MySQL connection refused
```bash
# Make sure MySQL is running
# On Windows: services.msc -> MySQL80
# On Mac: brew services list
# On Linux: sudo systemctl status mysql
```

### Issue: Dependencies not downloading
```bash
# Clear Maven cache and rebuild
rm -rf ~/.m2/repository/com/gvk
./mvnw clean install -U
```

## Next Steps

1. **Add Authentication**: Implement JWT-based authentication
2. **Add More Endpoints**: Create, Update, Delete operations
3. **Add Caching**: Implement Redis caching for better performance
4. **Add Pagination**: Implement proper pagination for large datasets
5. **Add Sorting**: Allow sorting by different fields
6. **Add Filtering**: More advanced filtering options

## Support

For issues and questions, please check the logs:
```bash
# View application logs
tail -f logs/gvk-healthhub.log
```

## License

Proprietary - GVK Health Hub
