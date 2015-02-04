begin;
drop table if exists oceny_uczniow cascade;
drop table if exists rodzaje_aktywnosci cascade;
drop table if exists uczniowie cascade;
drop table if exists uwagi cascade;
drop table if exists klasy cascade;
drop table if exists nauczyciele cascade;
drop table if exists przedmioty cascade;
drop table if exists plan_lekcji cascade;
drop table if exists przeprowadzone_lekcje cascade;
drop table if exists nieobecnosci cascade;
drop table if exists uzytkownicy cascade;

drop trigger if exists user_id_check on uczniowie;
drop trigger if exists user_id_check on nauczyciele;

drop function if exists check_ocena_przedmiot(integer,integer);
drop function if exists check_weekday(numeric(1,0));
drop function if exists check_nieobecnosc_przedmiot(integer,integer);
drop function if exists user_id_check();

commit;
