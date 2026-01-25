package org.example;

import java.util.List;
import java.util.Scanner;

public class CarRentalApp {
    private final Scanner scanner;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final RentalRepository rentalRepository;

    private String currentAppUser = null;
    private int currentAppUserId = -1;

    public CarRentalApp() {
        this.scanner = new Scanner(System.in);
        this.userRepository = new UserRepository();
        this.carRepository = new CarRepository();
        this.clientRepository = new ClientRepository();
        this.rentalRepository = new RentalRepository();
    }

    public void start() {
        System.out.println("=== SYSTEM WYPOŻYCZALNI SAMOCHODÓW ===");

        while (true) {
            System.out.println("\n--- EKRAN STARTOWY ---");
            System.out.println("Wybierz tryb (tożsamość bazy danych):");
            System.out.println("1. ADMIN (Pełny dostęp)");
            System.out.println("2. PRACOWNIK (Dostęp standardowy)");
            System.out.println("0. Wyjdź");
            System.out.print("> ");

            String choice = scanner.nextLine();

            if (choice.equals("0")) {
                System.out.println("Do widzenia!");
                break;
            } else if (choice.equals("1")) {
                handleAdminMode();
            } else if (choice.equals("2")) {
                handleEmployeeMode();
            } else {
                System.out.println("Nieznana opcja.");
            }
        }
    }

    private void handleAdminMode() {
        System.out.print("Podaj hasło ROOT do bazy danych: ");
        String rootPass = scanner.nextLine();

        DatabaseConnection.switchToAdminMode(rootPass);

        try (var test = DatabaseConnection.getConnection()) {
            System.out.println("Połączono jako ROOT.");

            userRepository.ensureAdminAccountExists(rootPass);
            currentAppUser = "admin";
            currentAppUserId = userRepository.getUserIdByUsername("admin");

            runAdminPanel();

        } catch (Exception e) {
            System.out.println("BŁĄD: Złe hasło do bazy danych lub brak dostępu!");
        }
    }

    private void handleEmployeeMode() {
        System.out.print("Podaj hasło techniczne pracownika (DB): ");
        String dbPass = scanner.nextLine();
        DatabaseConnection.switchToEmployeeMode(dbPass);

        try (var test = DatabaseConnection.getConnection()) {
            System.out.println("Połączono z bazą. Wymagane logowanie użytkownika.");

            System.out.print("Login: ");
            String login = scanner.nextLine();
            System.out.print("Hasło: ");
            String pass = scanner.nextLine();

            if (userRepository.loginUser(login, pass)) {
                currentAppUser = login;
                currentAppUserId = userRepository.getUserIdByUsername(login);
                runEmployeePanel();
            } else {
                System.out.println("Błąd logowania do aplikacji.");
            }
        } catch (Exception e) {
            System.out.println("BŁĄD: Nie można połączyć się z bazą (złe hasło techniczne?).");
        }
    }

    private void runAdminPanel() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== PANEL ADMINA [" + currentAppUser + "] ===");
            System.out.println("1. Zarządzaj Pracownikami (Dodaj/Usuń)");
            System.out.println("2. Zarządzaj Flotą (Dodaj/Usuń auto)");
            System.out.println("3. Zarządzaj Klientami (Dodaj/Usuń)");
            System.out.println("4. Wyszukaj/Przeglądaj auta");
            System.out.println("5. Panel Wypożyczania");
            System.out.println("0. Wyloguj (Powrót do startu)");
            System.out.print("> ");

            String cmd = scanner.nextLine();
            switch (cmd) {
                case "1": manageEmployees(); break;
                case "2": manageFleet(); break;
                case "3": manageClients(); break;
                case "4":
                    handleCarSearch();
                    break;
                case "5":
                    handleRentalMenu();
                    break;
                case "0": running = false; break;
                default: System.out.println("Nieznana opcja.");
            }
        }
    }

    private void runEmployeePanel() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== PANEL PRACOWNIKA [" + currentAppUser + "] ===");
            System.out.println("1. Wyszukaj/Przeglądaj auta");
            System.out.println("2. Dodaj/Edytuj Klienta");
            System.out.println("3. Panel Wypożyczania");
            System.out.println("0. Wyloguj");
            System.out.print("> ");

            String cmd = scanner.nextLine();
            switch (cmd) {
                case "1":
                    handleCarSearch();
                    break;
                case "2":
                    System.out.println("\n=== PANEL PRACOWNIKA [" + currentAppUser + "] ===");
                    System.out.println("1. Dodaj Klienta");
                    System.out.println("2. Edytuj Klienta(Tel/Adres)");
                    System.out.print("> ");
                    String clCmd = scanner.nextLine();
                    switch (clCmd){
                        case "1":
                            addClientInteractive();
                            break;
                        case "2":
                            clientRepository.getAllClients().forEach(System.out::println);
                            System.out.print("Podaj ID klienta do edycji: ");
                            int id = Integer.parseInt(scanner.nextLine());

                            System.out.print("Nowy telefon: ");
                            String phone = scanner.nextLine();

                            System.out.print("Nowy adres: ");
                            String addr = scanner.nextLine();

                            clientRepository.updateClient(id, phone, addr);
                            break;
                    }

                    break;
                case "3":
                    handleRentalMenu();
                    break;
                case "0": running = false; break;
                default: System.out.println("Nieznana opcja.");
            }
        }
    }

    private void handleCarSearch(){
        System.out.println("\n--- BAZA SAMOCHODÓW ---");
        System.out.println("1. Pokaż wszystkie dostępne");
        System.out.println("2. Wyszukaj konkretne auto (Marka/Model)");
        System.out.print("Wybierz: ");

        String searchOption = scanner.nextLine();

        if (searchOption.equals("1")) {
            carRepository.getCars("Dostepny").forEach(System.out::println);
        } else if (searchOption.equals("2")) {
            System.out.print("Wpisz frazę: ");
            String keyword = scanner.nextLine();
            carRepository.searchCars(keyword).forEach(System.out::println);
        } else {
            System.out.println("Niepoprawny wybór.");
        }
    }

    private void manageEmployees() {
        System.out.println("\n1. Lista pracowników\n2. Dodaj nowego\n3. Usuń pracownika");
        System.out.print("> ");
        String sub = scanner.nextLine();
        if (sub.equals("1")) {
            userRepository.getAllEmployees().forEach(System.out::println);
        } else if (sub.equals("2")) {
            System.out.print("Nowy login: ");
            String l = scanner.nextLine();
            System.out.print("Nowe hasło: ");
            String p = scanner.nextLine();
            userRepository.addEmployee(l, p);
        } else if (sub.equals("3")) {
            System.out.print("Podaj login do usunięcia: ");
            String l = scanner.nextLine();
            userRepository.deleteEmployee(l, currentAppUser);
        }
    }

    private void manageFleet() {
        System.out.println("\n--- ZARZĄDZANIE FLOTĄ ---");
        System.out.println("1. Dodaj konkretne Auto (fizyczny egzemplarz)");
        System.out.println("2. Usuń Auto");
        System.out.println("3. Dodaj nową Markę (np. BMW)");
        System.out.println("4. Dodaj nowy Model (np. X5)");
        System.out.println("5. Wyślij auto na serwis");
        System.out.println("6. Pzywróć auto z serwisu");
        System.out.print("Wybierz: ");

        String sub = scanner.nextLine();

        if (sub.equals("1")) {
            System.out.println("Dodawanie auta");
            System.out.println("Wybierz ID Modelu z listy poniżej:");
            carRepository.showAllModels();

            System.out.print("ID Modelu: ");
            int mId = Integer.parseInt(scanner.nextLine());

            System.out.print("Rejestracja: ");
            String reg = scanner.nextLine();

            System.out.print("Rok: ");
            int year = Integer.parseInt(scanner.nextLine());

            System.out.print("Aktualny przebieg (km): ");
            int km = Integer.parseInt(scanner.nextLine());

            carRepository.addCar(mId, reg, year, km);

        } else if (sub.equals("2")) {
            carRepository.getAllCars().forEach(System.out::println);
            System.out.print("Podaj ID auta do usunięcia: ");
            int id = Integer.parseInt(scanner.nextLine());
            carRepository.deleteCar(id);

        } else if (sub.equals("3")) {
            carRepository.showAllBrands();
            System.out.print("Podaj nazwę nowej marki: ");
            String brand = scanner.nextLine();
            carRepository.addBrand(brand);

        } else if (sub.equals("4")) {
            carRepository.showAllBrands();

            System.out.print("Podaj ID Marki z powyższych: ");
            int bId = Integer.parseInt(scanner.nextLine());

            System.out.print("Nazwa modelu (np. Corolla): ");
            String mName = scanner.nextLine();

            System.out.print("Kategoria (Sedan/SUV/Kombi): ");
            String cat = scanner.nextLine();

            System.out.print("Cena za dobę (PLN): ");
            double rate = Double.parseDouble(scanner.nextLine());

            carRepository.addModel(bId, mName, cat, rate);
        } else if (sub.equals("5")) {
            carRepository.getCars("Dostepny").forEach(System.out::println);
            System.out.print("Podaj ID samochodu do wysłania: ");
            int id = Integer.parseInt(scanner.nextLine());
            carRepository.sendToService(id);
        } else if (sub.equals("6")) {
            carRepository.getCars("Serwis").forEach(System.out::println);
            System.out.print("Podaj ID samochodu do przywrócenia: ");
            int id = Integer.parseInt(scanner.nextLine());
            carRepository.getFromService(id);
        }
    }

    private void manageClients() {
        System.out.println("\n--- ZARZĄDZANIE KLIENTAMI ---");
        System.out.println("1. Wyświetl wszystkich");
        System.out.println("2. Usuń klienta");
        System.out.println("3. Edytuj klienta (Tel/Adres)");
        System.out.print("Wybierz: ");

        String sub = scanner.nextLine();

        if (sub.equals("1")) {
            clientRepository.getAllClients().forEach(System.out::println);

        } else if (sub.equals("2")) {
            System.out.print("Podaj ID klienta do usunięcia: ");
            int id = Integer.parseInt(scanner.nextLine());
            clientRepository.deleteClient(id);

        } else if (sub.equals("3")) {
            clientRepository.getAllClients().forEach(System.out::println);
            System.out.print("Podaj ID klienta do edycji: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Nowy telefon: ");
            String phone = scanner.nextLine();

            System.out.print("Nowy adres: ");
            String addr = scanner.nextLine();

            clientRepository.updateClient(id, phone, addr);
        }
    }

    private void addClientInteractive() {
        System.out.println("--- Dodawanie Klienta ---");
        System.out.print("Imię: "); String name = scanner.nextLine();
        System.out.print("Nazwisko: "); String sur = scanner.nextLine();
        System.out.print("Numer prawa jazdy: "); String npj = scanner.nextLine();
        System.out.print("Tel: "); String phone = scanner.nextLine();
        System.out.print("Adres: "); String address = scanner.nextLine();
        clientRepository.addClient(name, sur, npj, phone, address);
    }

    private void handleRentalMenu() {
        System.out.println("\n--- STREFA WYPOŻYCZEŃ ---");
        System.out.println("1. Wydaj samochód (Wypożyczenie)");
        System.out.println("2. Odbierz samochód (Zwrot)");
        System.out.print("Wybierz: ");
        String sub = scanner.nextLine();

        if (sub.equals("1")) {
            System.out.println("\n[KROK 1] Wybierz auto (tylko dostępne)");
            carRepository.getCars("Dostepny").forEach(System.out::println);
            System.out.print("Podaj ID auta: ");
            int carId = Integer.parseInt(scanner.nextLine());

            System.out.println("\n[KROK 2] Wybierz klienta");
            clientRepository.getAllClients().forEach(System.out::println);
            System.out.print("Podaj ID klienta: ");
            int clientId = Integer.parseInt(scanner.nextLine());
            System.out.print("Podaj ilość dni wypożyczenia: ");
            int days = Integer.parseInt(scanner.nextLine());

            rentalRepository.rentCar(clientId, carId, currentAppUserId, days);

        } else if (sub.equals("2")) {
            carRepository.getCars("Wypozyczony").forEach(System.out::println);
            System.out.print("\nPodaj ID zwracanego auta: ");
            int carId = Integer.parseInt(scanner.nextLine());

            System.out.print("Podaj aktualny przebieg (km): ");
            int mileage = Integer.parseInt(scanner.nextLine());

            rentalRepository.returnCar(carId, mileage);
        } else {
            System.out.println("Anulowano.");
        }
    }

    public static void main(String[] args) {
        new CarRentalApp().start();
    }
}