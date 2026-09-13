Write-Host "Building Online Reservation System..."
mvn clean compile
if ($LASTEXITCODE -eq 0) {
    Write-Host "Starting application..."
    mvn exec:java
}
