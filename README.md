# Cardiac Trader - Backend

A Spring Boot REST API backend for the Cardiac Trader stock trading game. Built with Spring Boot 3.5.7, Java 17, MySQL, and Spring Security.

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- (Optional) API keys for external services:
  - Alpha Vantage API key
  - Open Exchange Rates API key
  - NewsAPI key

### Installation

1. **Clone and navigate to the backend directory**
   ```bash
   cd cardiac-trader-backend
   ```

2. **Configure the database**
   - Copy the example configuration:
     ```bash
     cp src/main/resources/application.properties.example src/main/resources/application.properties
     ```
   - Edit `src/main/resources/application.properties`:
     - Set MySQL connection details
     - Configure JWT secret key
     - (Optional) Add external API keys
     - Configure email SMTP settings

3. **Create the database**
   - MySQL will create the database automatically if `createDatabaseIfNotExist=true` is set
   - Or create manually:
     ```sql
     CREATE DATABASE cardiac_trader;
     ```

4. **Build and run**
   ```bash
   # Using Maven wrapper (Windows)
   mvnw.cmd spring-boot:run
   
   # Using Maven wrapper (Linux/Mac)
   ./mvnw spring-boot:run
   
   # Or using Maven directly
   mvn spring-boot:run
   ```

The API will be available at http://localhost:8080

## 📁 Project Structure

```
cardiac-trader-backend/
├── src/
│   ├── main/
│   │   ├── java/com/scu/uob/dsa/cardiac_trader_backend/
│   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── DataInitializer.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebClientConfig.java
│   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── GameController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── MarketController.java
│   │   │   │   ├── ToolController.java
│   │   │   │   ├── TradingController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── enums/          # Enumeration types
│   │   │   ├── exception/      # Exception handlers
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic
│   │   │   └── util/           # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application.properties.example
│   │       └── templates/email/  # Email templates
│   └── test/                   # Test classes
├── pom.xml                     # Maven dependencies
└── mvnw                        # Maven wrapper
```

## 🎯 API Endpoints

### Authentication (`/api/auth`)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/validate` - Validate token
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password
- `POST /api/auth/verify-email` - Verify email address
- `POST /api/auth/resend-verification` - Resend verification email
- `GET /api/auth/profile` - Get user profile
- `PUT /api/auth/profile` - Update user profile
- `POST /api/auth/change-password` - Change password

### Game (`/api/game`)
- `POST /api/game/start` - Start new game session
- `GET /api/game/session` - Get current game session
- `POST /api/game/round/start` - Start a round
- `POST /api/game/round/complete` - Complete a round

### Trading (`/api/trading`)
- `POST /api/trading/buy` - Buy stocks
- `POST /api/trading/sell` - Sell stocks
- `GET /api/trading/portfolio` - Get portfolio

### Market (`/api/market`)
- `GET /api/market/stocks` - Get available stocks
- `POST /api/market/update-prices` - Update market prices

### Tools (`/api/tools`)
- `POST /api/tools/hint` - Use hint tool
- `POST /api/tools/time-boost` - Use time boost tool
- `GET /api/tools/available` - Check tool availability

### Health (`/api/health`)
- `GET /api/health` - Health check endpoint

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI JSON**: http://localhost:8080/api-docs

## ⚙️ Configuration

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cardiac_trader?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### JWT Configuration
```properties
jwt.secret=your-256-bit-secret-key-change-this-in-production
jwt.expiration=86400000  # 24 hours
jwt.refresh.expiration=604800000  # 7 days
```

### CORS Configuration
```properties
cors.allowed.origins=http://localhost:3000,http://localhost:5173,http://localhost:8081
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
```

### External APIs (Optional)
```properties
# Heart Game API (Required)
heart.api.url=https://marcconrad.com/uob/heart

# Alpha Vantage API (Optional)
alpha.vantage.api.key=your_key

# Open Exchange Rates API (Optional)
exchange.rates.api.key=your_key

# NewsAPI (Optional)
news.api.key=your_key
```

### Email Configuration
```properties
app.email.enabled=true
app.email.from=your-email@gmail.com
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

**Note**: For Gmail, use an App Password (not your regular password). Get it from: Google Account → Security → 2-Step Verification → App passwords

## 🛠️ Technology Stack

- **Spring Boot 3.5.7**: Application framework
- **Java 17**: Programming language
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **MySQL**: Relational database
- **JWT (jjwt 0.12.5)**: Token-based authentication
- **SpringDoc OpenAPI**: API documentation
- **Lombok**: Boilerplate code reduction
- **Spring Mail**: Email functionality
- **Spring WebFlux**: Reactive HTTP client for external APIs

## 🗄️ Database Schema

The application uses the following main entities:
- **User**: User accounts and authentication
- **GameSession**: Game sessions with difficulty levels
- **Round**: Individual trading rounds
- **Stock**: Available stocks for trading
- **Holding**: User stock holdings
- **Transaction**: Trading transaction history

## 🔐 Security

- **JWT Authentication**: Token-based authentication with httpOnly cookies
- **Password Encryption**: BCrypt password hashing
- **CORS**: Configurable cross-origin resource sharing
- **Email Verification**: Email verification for account security
- **Password Reset**: Secure password reset via email

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## 📦 Building

### Development
```bash
./mvnw spring-boot:run
```

### Production Build
```bash
./mvnw clean package
```
The JAR file will be in `target/cardiac-trader-backend-0.0.1-SNAPSHOT.jar`

### Run JAR
```bash
java -jar target/cardiac-trader-backend-0.0.1-SNAPSHOT.jar
```

## 🔧 Development

### Logging
Logs are written to:
- Console (INFO level)
- File: `logs/cardiac-trader-backend.log`

### Database Initialization
The `DataInitializer` class automatically initializes:
- Default stocks
- Sample data (if needed)

### External API Integration
- **Heart Game API**: Provides heart puzzle images
- **Alpha Vantage**: Market trend data (optional)
- **Open Exchange Rates**: Currency fluctuations (optional)
- **NewsAPI**: Market news events (optional)

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running: `mysql -u root -p`
   - Check connection URL in `application.properties`
   - Ensure database exists or `createDatabaseIfNotExist=true`

2. **Port Already in Use**
   - Change port in `application.properties`: `server.port=8081`
   - Or stop the process using port 8080

3. **JWT Token Errors**
   - Verify `jwt.secret` is set in `application.properties`
   - Ensure secret is at least 256 bits
   - Check token expiration settings

4. **CORS Errors**
   - Add frontend URL to `cors.allowed.origins`
   - Verify CORS configuration in `CorsConfig.java`

5. **Email Not Sending**
   - Check SMTP settings in `application.properties`
   - For Gmail, use App Password (16 characters)
   - Verify `app.email.enabled=true`
   - Check firewall/network settings

6. **External API Errors**
   - Verify API keys are correct
   - Check network connectivity
   - Review API rate limits
   - Check logs for specific error messages

## 📝 Environment Variables

You can override configuration using environment variables:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/cardiac_trader
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=your_password

# JWT
export JWT_SECRET=your-secret-key

# External APIs
export HEART_API_URL=https://marcconrad.com/uob/heart
export ALPHA_VANTAGE_API_KEY=your_key
export EXCHANGE_RATES_API_KEY=your_key
export NEWS_API_KEY=your_key
```

## 🔗 Related Documentation

- [Frontend README](../cardiac_trader_frontend/README.md)
- [Main README](../README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)

## 📄 License

Part of the UOB-SCU DSA course project.

