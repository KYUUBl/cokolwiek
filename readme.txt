Nasz projekt to szkolny dziennik elektroniczny. 
Z dziennika mog¹ korzystaæ uczniowie, nauczyciele oraz admin(dyrektor),
Uczniowie mog¹ przegladac swoje oceny, plan zajêæ itp,
nauczyciele mog¹ dodawaæ oceny, przeprowadzone lekcje, nieobecnosci itp.
Dyrektor moze dodawaæ uzytkownikow dla istniejacych uczniow/nauczycieli, dodawac nowe klasy,przedmiotynauczycieli,dezaktywowac uczniow(np tych ktorzy juz ukonczyli szkole) itp,

Wymagania:
-Java 8,
-PostgreSQL JDBC Driver for Java 8
https://jdbc.postgresql.org/download/postgresql-9.4-1200.jdbc41.jar

Jak postawiæ:
postawiæ baze danych z create.sql,
w klasie constants.java ustawic adres bazy danych(DB_ADRESS), nazwe uzytkownika(login), has³o(password),
skompilowaæ i odpaliæ
