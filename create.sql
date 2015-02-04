begin;
CREATE TABLE nauczyciele (
	id                   serial  NOT NULL,
	imie                 varchar(20)  NOT NULL,
	nazwisko             varchar(20)  ,
	id_uzytkownika	     varchar(20),
	CONSTRAINT pk_nauczyciele PRIMARY KEY ( id ),
	CONSTRAINT idx_id_uzytkownika UNIQUE ( id_uzytkownika )
 );

CREATE TABLE rodzaje_aktywnosci (
	id                   serial  NOT NULL,
	nazwa                varchar(20)  NOT NULL,
	waga                 numeric(1,0)  NOT NULL,
	CONSTRAINT pk_rodzajeaktywnosci PRIMARY KEY ( id ),
	CONSTRAINT idx_nazwa_0 UNIQUE ( nazwa )
 );

CREATE TABLE klasy (
	id                   serial  NOT NULL,
	oddzial              char(1)  NOT NULL,
	rok_rozpoczecia	     numeric(4,0) NOT NULL,
	id_wychowawcy        serial  NOT NULL,
	CONSTRAINT pk_klasy PRIMARY KEY ( id ),
	CONSTRAINT idx_klasy UNIQUE ( id_wychowawcy )
 );


CREATE TABLE przedmioty (
	id                   serial  NOT NULL,
	nazwa                varchar(15)  NOT NULL,
	id_prowadzacego      serial  NOT NULL,
	id_klasy             serial  NOT NULL,
	aktywny		     bool DEFAULT TRUE NOT NULL,
	CONSTRAINT pk_przedmioty PRIMARY KEY ( id )
 );

CREATE INDEX idx_przedmioty ON przedmioty ( id_klasy );

CREATE INDEX idx_przedmioty_0 ON przedmioty ( id_prowadzacego );

CREATE TABLE uczniowie (
	imie                 varchar(20)  NOT NULL,
	nazwisko             varchar(20)  NOT NULL,
	telefon_do_rodzica   numeric(9,0) ,
	pesel                char(11) NOT NULL,
	id_klasy             serial  NOT NULL,
	id_uzytkownika	     varchar(20),
	aktywny		     bool DEFAULT TRUE NOT NULL,
	CONSTRAINT pk_uczniowie PRIMARY KEY ( pesel ),
	CONSTRAINT idx_id_uuzytkownika UNIQUE ( id_uzytkownika )
 );

ALTER TABLE uczniowie ADD CONSTRAINT ck_pesel check(char_length(pesel)=11 and (floor(to_number(pesel,'99999999999')/10000000000)%10 + floor(to_number(pesel,'99999999999')/1000000000)%10*3 + floor(to_number(pesel,'99999999999')/100000000)%10*7 + floor(to_number(pesel,'99999999999')/10000000)%10*9 + floor(to_number(pesel,'99999999999')/1000000)%10 + floor(to_number(pesel,'99999999999')/100000)%10*3 + floor(to_number(pesel,'99999999999')/10000)%10*7 + floor(to_number(pesel,'99999999999')/1000)%10*9 + floor(to_number(pesel,'99999999999')/100)%10 + floor(to_number(pesel,'99999999999')/10)%10*3 + to_number(pesel,'99999999999')%10)%10 = 0);

ALTER TABLE uczniowie ADD CONSTRAINT ck_telefon check(telefon_do_rodzica between 100000000 and 999999999);

CREATE INDEX idx_uczniowie ON uczniowie ( id_klasy );

CREATE TABLE uwagi (
	id                   serial  NOT NULL,
	id_ucznia            char(11)  NOT NULL,
	id_nauczyciela       serial  NOT NULL,
	opis                 varchar(300)  NOT NULL,
	czy_pozytywna        bool DEFAULT true NOT NULL,
	data_wystawienia     date DEFAULT current_date NOT NULL,
	CONSTRAINT pk_uwagi PRIMARY KEY ( id )
 );

ALTER TABLE uwagi ADD CONSTRAINT ck_0 CHECK ( data_wystawienia <= now() );

CREATE TABLE oceny_uczniow (
	id                   serial  NOT NULL,
	wartosc              numeric(1,0)  NOT NULL,
	id_ucznia            char(11)  NOT NULL,
	id_przedmiotu        serial  NOT NULL,
	id_aktywnosci        serial  NOT NULL,
	tematyka	     varchar(20) NOT NULL,
	CONSTRAINT pk_oceny_uczniow PRIMARY KEY ( id )
 );

CREATE TABLE plan_lekcji (
	id                   serial  NOT NULL,
	id_przedmiotu        serial  NOT NULL,
	nr_lekcji            numeric(1,0)  NOT NULL,
	dzien_tygodnia       numeric(1,0)  NOT NULL,
	CONSTRAINT pk_plan_lekcji PRIMARY KEY ( id )
 );

CREATE INDEX idx_plan_lekcji ON plan_lekcji ( id_przedmiotu );

CREATE TABLE przeprowadzone_lekcje (
	id                   serial  NOT NULL,
	"data"               date DEFAULT current_date NOT NULL,
	temat_zajec          varchar(100)  ,
	id_prowadzacego      serial  NOT NULL,
	id_lekcji            serial  NOT NULL,
	CONSTRAINT pk_lekcje PRIMARY KEY ( id )
 );

ALTER TABLE przeprowadzone_lekcje ADD CONSTRAINT ck_1 CHECK ( data <= now() );

CREATE INDEX idx_Lekcje ON przeprowadzone_lekcje ( id_prowadzacego );

CREATE INDEX idx_lekcje_0 ON przeprowadzone_lekcje ( id_lekcji );

CREATE TABLE nieobecnosci (
	id_ucznia            char(11)  NOT NULL,
	id_lekcji            serial  NOT NULL,
	CONSTRAINT idx_nieobecnosci_1 PRIMARY KEY ( id_ucznia, id_lekcji )
 );

CREATE TABLE uzytkownicy (
	login 		     varchar(20) NOT NULL,
	haslo		     varchar(20) NOT NULL,
	CONSTRAINT pk_uzytkownicy PRIMARY KEY ( login )
 );

CREATE OR REPLACE FUNCTION check_weekday(i numeric(1,0)) RETURNS numeric(1,0)
	AS $$ select dzien_tygodnia from plan_lekcji where i=id $$
	LANGUAGE SQL;

CREATE OR REPLACE FUNCTION check_ocena_przedmiot(id_przedm integer, id_u char) RETURNS bool
	AS $$ select (select id_klasy from przedmioty where id_przedm=id) = (select id_klasy from uczniowie where id_u=pesel) $$
	LANGUAGE SQL;

CREATE OR REPLACE FUNCTION check_nieobecnosc_przedmiot(id_lek integer, id_u char) RETURNS bool
	AS $$ select (select id_klasy from uczniowie where id_u=pesel) = (select id_klasy from przeprowadzone_lekcje prl join plan_lekcji pl on id_lekcji=pl.id join przedmioty p on id_przedmiotu=p.id where prl.id=id_lek) $$
	LANGUAGE SQL;

ALTER TABLE przeprowadzone_lekcje ADD CONSTRAINT ck_weekday CHECK( to_number(to_char("data", 'D'),'9')=check_weekday(id_lekcji));

ALTER TABLE oceny_uczniow ADD CONSTRAINT ck_oceny_przedmioty CHECK ( check_ocena_przedmiot(id_przedmiotu,id_ucznia) );

ALTER TABLE nieobecnosci ADD CONSTRAINT ck_niebecnosci_przedmioty CHECK ( check_nieobecnosc_przedmiot(id_lekcji,id_ucznia) );

ALTER TABLE klasy ADD CONSTRAINT fk_klasy_nauczyciele FOREIGN KEY ( id_wychowawcy ) REFERENCES nauczyciele( id );

ALTER TABLE nieobecnosci ADD CONSTRAINT fk_nieobecnosci_uczniowie FOREIGN KEY ( id_ucznia ) REFERENCES uczniowie( pesel ) on delete cascade;

ALTER TABLE nieobecnosci ADD CONSTRAINT fk_nieobecnosci_lekcje FOREIGN KEY ( id_lekcji ) REFERENCES przeprowadzone_lekcje( id ) on delete cascade;

ALTER TABLE oceny_uczniow ADD CONSTRAINT fk_oceny_uczniow_uczniowie FOREIGN KEY ( id_ucznia ) REFERENCES uczniowie( pesel ) on delete cascade;

ALTER TABLE oceny_uczniow ADD CONSTRAINT fk_oceny_uczniow_przedmioty FOREIGN KEY ( id_przedmiotu ) REFERENCES przedmioty( id );

ALTER TABLE oceny_uczniow ADD CONSTRAINT fk_oceny_uczniow_rodzaje_aktywnosci FOREIGN KEY ( id_aktywnosci ) REFERENCES rodzaje_aktywnosci( id );

ALTER TABLE plan_lekcji ADD CONSTRAINT fk_plan_lekcji_przedmioty FOREIGN KEY ( id_przedmiotu ) REFERENCES przedmioty( id ) on delete cascade;

ALTER TABLE przedmioty ADD CONSTRAINT fk_przedmioty_klasy FOREIGN KEY ( id_klasy ) REFERENCES klasy( id ) on delete cascade;

ALTER TABLE przedmioty ADD CONSTRAINT fk_przedmioty_nauczyciele FOREIGN KEY ( id_prowadzacego ) REFERENCES nauczyciele( id );

ALTER TABLE przeprowadzone_lekcje ADD CONSTRAINT fk_lekcje_nauczyciele FOREIGN KEY ( id_prowadzacego ) REFERENCES nauczyciele( id );

ALTER TABLE przeprowadzone_lekcje ADD CONSTRAINT fk_lekcje_plan_lekcji FOREIGN KEY ( id_lekcji ) REFERENCES plan_lekcji( id ) on delete cascade;

ALTER TABLE uczniowie ADD CONSTRAINT fk_uczniowie_klasy FOREIGN KEY ( id_klasy ) REFERENCES klasy( id ) on delete cascade;

ALTER TABLE uwagi ADD CONSTRAINT fk_uwagi_nauczyciele FOREIGN KEY ( id_nauczyciela ) REFERENCES nauczyciele( id );

ALTER TABLE uwagi ADD CONSTRAINT fk_uwagi_uczniowie FOREIGN KEY ( id_ucznia ) REFERENCES uczniowie( pesel ) on delete cascade;

ALTER TABLE uczniowie ADD CONSTRAINT fk_uczniowie_uzytkownicy FOREIGN KEY ( id_uzytkownika ) REFERENCES uzytkownicy( login ) on delete cascade;

ALTER TABLE nauczyciele ADD CONSTRAINT fk_nauczyciele_uzytkownicy FOREIGN KEY ( id_uzytkownika ) REFERENCES uzytkownicy( login ) on delete cascade;

CREATE OR REPLACE FUNCTION user_id_check() returns trigger AS $user_id_check$
BEGIN
	IF ( new.id_uzytkownika in (select id_uzytkownika from uczniowie) or new.id_uzytkownika in (select id_uzytkownika from nauczyciele)) 
	THEN RAISE EXCEPTION 'niepoprawne id_uzytkownika';
	END IF;
	RETURN NEW;
END;
$user_id_check$ LANGUAGE plpgsql;

CREATE TRIGGER user_id_check BEFORE INSERT OR UPDATE ON uczniowie
FOR EACH ROW EXECUTE PROCEDURE user_id_check();

CREATE TRIGGER user_id_check BEFORE INSERT OR UPDATE ON nauczyciele
FOR EACH ROW EXECUTE PROCEDURE user_id_check();

--przykladowe dane:

-- INSERT INTO rodzaje_aktywnosci (nazwa, waga) VALUES
-- ('sprawdzian', 5),
-- ('kartkowka', 2),
-- ('odpowiedz ustna', 3),
-- ('aktywnosc lekcyjna', 1),
-- ('zadanie domowe', 2);

-- INSERT INTO nauczyciele (imie, nazwisko) VALUES
-- ('Llrbbmqb', 'Mdarzow'),
-- ('Liddq', 'Mdxrjmow'),
-- ('Gjybld', 'Jfsarcbynec'),
-- ('Vxxpklo', 'Vllnmpapqfw'),
-- ('Mkmcoqh', 'Enkuewhsqmg'),
-- ('Jcljjiv', 'Cmdkqt'),
-- ('Tmvtr', 'Vljptnsnf'),
-- ('Ijmafad', 'Twsofsbcn'),
-- ('Bffbsaq', 'Apqcace'),
-- ('Qvfrkmln', 'Ajkpqpxrjx'),
-- ('Lyxacbhh', 'Gcqcoend'),
-- ('Sgdwd', 'Acgpxiqvku'),
-- ('Fcgdewht', 'Siohordtqk'),
-- ('Tgspq', 'Hmsboaguwn'),
-- ('Vnzlg', 'Twpbtrw'),
-- ('Sadeug', 'Pmoqcd'),
-- ('Stokyxho', 'Rhwdvmxx'),
-- ('Exlmnd', 'Cukwag'),
-- ('Guukw', 'Abxubume'),
-- ('Yatdrmy', 'Tajxlog');

-- INSERT INTO klasy (oddzial, rocznik,rok_rozpoczecia, id_wychowawcy) VALUES
-- ('A', 1, 2006, 1),
-- ('B', 1, 2006, 2),
-- ('A', 2, 2005, 4),
-- ('B', 2, 2005, 5),
-- ('A', 3, 2004, 3),
-- ('B', 3, 2004, 6);

-- INSERT INTO przedmioty (nazwa, id_prowadzacego, id_klasy) VALUES
-- ('Matematyka', 1, 1),
-- ('Matematyka', 1, 2),
-- ('Matematyka', 1, 3),
-- ('Matematyka', 1, 4),
-- ('Matematyka', 1, 5),
-- ('Matematyka', 1, 6),
-- ('Jezyk polski', 2, 1),
-- ('Jezyk polski', 2, 2),
-- ('Jezyk polski', 2, 3),
-- ('Jezyk polski', 2, 4),
-- ('Jezyk polski', 2, 5),
-- ('Jezyk polski', 2, 6),
-- ('WF', 3, 1),
-- ('WF', 3, 2),
-- ('WF', 3, 3),
-- ('WF', 3, 4),
-- ('Chemia', 5, 1),
-- ('Chemia', 5, 3),
-- ('Chemia', 4, 5),
-- ('Biologia', 5, 1),
-- ('Biologia', 5, 3),
-- ('Biologia', 4, 5),
-- ('Informatyka', 6, 2),
-- ('Informatyka', 6, 4),
-- ('Informatyka', 6, 6),
-- ('Fizyka', 6, 2),
-- ('Fizyka', 6, 4),
-- ('Fizyka', 6, 6),
-- ('Jezyk angielski', 7, 1),
-- ('Jezyk angielski', 7, 2),
-- ('Jezyk angielski', 8, 3),
-- ('Jezyk angielski', 8, 4),
-- ('Jezyk angielski', 9, 5),
-- ('Jezyk angielski', 9, 6),
-- ('Religia', 10, 1),
-- ('Religia', 10, 2),
-- ('Religia', 10, 3),
-- ('Religia', 11, 4),
-- ('Religia', 11, 5),
-- ('Religia', 11, 6);

-- INSERT INTO uczniowie (imie, nazwisko, telefon_do_rodzica, pesel, id_klasy) VALUES
-- ('Llrbbmqb', 'Mdarzow', 140383426, 96091227824, 5),
-- ('Liddq', 'Mdxrjmow', 169133069, 95080364878, 4),
-- ('Gjybld', 'Jfsarcbynec', 149241873, 96080786581, 3),
-- ('Vxxpklo', 'Vllnmpapqfw', 185990364, 95010881893, 4),
-- ('Mkmcoqh', 'Enkuewhsqmg', 156297539, 96061849746, 4),
-- ('Jcljjiv', 'Cmdkqt', 194953865, 95112464312, 6),
-- ('Tmvtr', 'Vljptnsnf', 127254586, 96121214718, 6),
-- ('Ijmafad', 'Twsofsbcn', 117142618, 95121345237, 2),
-- ('Bffbsaq', 'Apqcace', 108936987, 96112772823, 3),
-- ('Qvfrkmln', 'Ajkpqpxrjx', 176065818, 95041617599, 3),
-- ('Lyxacbhh', 'Gcqcoend', 104569917, 95012352911, 1),
-- ('Sgdwd', 'Acgpxiqvku', 120388464, 95072542383, 2),
-- ('Fcgdewht', 'Siohordtqk', 146811305, 95033043384, 1),
-- ('Tgspq', 'Hmsboaguwn', 131419379, 96102639141, 5),
-- ('Vnzlg', 'Twpbtrw', 160152959, 95102874947, 4),
-- ('Sadeug', 'Pmoqcd', 149517445, 95072878983, 1),
-- ('Stokyxho', 'Rhwdvmxx', 196864819, 95101912891, 2),
-- ('Exlmnd', 'Cukwag', 131602422, 95010444377, 6),
-- ('Guukw', 'Abxubume', 146340713, 96031111259, 3),
-- ('Yatdrmy', 'Tajxlog', 146247255, 96101136953, 5),
-- ('Emzhlvi', 'Louvsuy', 114723506, 95041512269, 3),
-- ('Ryulye', 'Xuoteh', 129033333, 95032259784, 2),
-- ('Pcfsk', 'Agkbbipzz', 178012497, 96030623564, 6),
-- ('Lxaml', 'Hfykgruo', 196060028, 96082215155, 2),
-- ('Xooobpp', 'Cqlwphapjna', 128104339, 96021036696, 3),
-- ('Ccnvwdtx', 'Cmyppph', 161717988, 96051382936, 1),
-- ('Sspusgdh', 'Hxqmbfjx', 175526309, 96092833275, 1),
-- ('Vdjsuy', 'Hyebmwsiqy', 130634994, 95051864411, 5),
-- ('Dxymz', 'Bypzvjegeb', 105193512, 95100458567, 3),
-- ('Eufts', 'Qixtigsieeh', 151300606, 96031317839, 5),
-- ('Mdflil', 'Xqfnxztqr', 147149314, 95012953891, 4),
-- ('Apkyhs', 'Ibppkq', 169110699, 95052642234, 2),
-- ('Tbuotbbq', 'Pivrfx', 172796157, 95060591234, 5),
-- ('Wddntgei', 'Adgaijvw', 173785404, 95072417689, 3),
-- ('Sbwew', 'Cvygehljx', 135889744, 95111757574, 4),
-- ('Ciwuqzdz', 'Ldubzvaf', 159343768, 96070275499, 4),
-- ('Fqwuzif', 'Bvyddwyv', 146478179, 96022821662, 6),
-- ('Aczmgyj', 'Kdxvtnun', 130449291, 95110386759, 3),
-- ('Rsplwui', 'Vfxlzb', 155722604, 95012088724, 2),
-- ('Awppan', 'Ycfirjcddso', 190127955, 95020374257, 1),
-- ('Legurfw', 'Wfmoxeqm', 110901063, 95061034769, 6),
-- ('Frghwlk', 'Kmeahk', 164945486, 95112693987, 3),
-- ('Jaehhsv', 'Wmqpxhlr', 103591171, 95040663263, 3),
-- ('Sfdzrh', 'Dsjeuygaf', 117076376, 95100358126, 1),
-- ('Ctpnimuw', 'Oqsjxvk', 111671338, 95012973118, 6),
-- ('Rxxvrwc', 'Asneogv', 170973813, 96082581696, 4),
-- ('Glpgdir', 'Pcriqifpg', 108399134, 95072279579, 4),
-- ('Prefxsn', 'Bcftpwctg', 130313563, 95091673574, 5),
-- ('Lnupycf', 'Buqunu', 151538839, 96120624512, 6),
-- ('Liitnck', 'Kfszbexra', 167107722, 95040785541, 4),
-- ('Hvhqndd', 'Kqvuygpnk', 138498976, 95012325779, 2),
-- ('Grpjv', 'Cxdpcwmjob', 160975266, 95042836962, 5),
-- ('Dkfojne', 'Ugxnno', 195466127, 95081373343, 1),
-- ('Swjwnn', 'Hwjckdmeouu', 102550399, 95021079236, 4),
-- ('Xhgvwuj', 'Wxxpitc', 134576987, 96111026657, 3),
-- ('Qaiddvh', 'Idsycqhklee', 158136104, 96011677676, 4),
-- ('Qembaqwq', 'Yqhsue', 134660183, 95010473528, 4),
-- ('Kgvjwd', 'Gjafqzzxlcx', 150099355, 96120359669, 6),
-- ('Sqgjla', 'Ipkvxfgvi', 125084100, 96072599191, 3),
-- ('Omkbljop', 'Bqvvhbgs', 130365981, 95070387425, 4),
-- ('Lhesnkq', 'Cwrqidr', 116945487, 96053033335, 5),
-- ('Hubbry', 'Qheyen', 155843485, 95032677397, 1),
-- ('Fbdeyq', 'Tgluaiihve', 121303708, 96032298331, 2),
-- ('Hjrqopu', 'Qguxhxdipfz', 155843024, 95062139456, 1),
-- ('Lbgfylq', 'Nzharvrlyau', 172312086, 96071567719, 2),
-- ('Pcnjkp', 'Wlffrkeecbp', 193552063, 96051296837, 5),
-- ('Mfhidj', 'Tjhrnxcx', 167974802, 96022448858, 2),
-- ('Oohqanx', 'Dmgzebhnlmw', 129834447, 96052212678, 1),
-- ('Hdvths', 'Bueeexg', 104665417, 95112182278, 3),
-- ('Ugskmv', 'Igfwvrftwap', 151201745, 96032315157, 6),
-- ('Vpbztyg', 'Prxajjngcom', 199885196, 95041756795, 3),
-- ('Nsdwss', 'Qovdruy', 144169939, 96121463686, 5),
-- ('Gulkfu', 'Nxnafamespc', 131190952, 95103033497, 4),
-- ('Vzxdr', 'Agyrqsc', 147407330, 96030586551, 2),
-- ('Nnvqqcq', 'Eitlvcnv', 191714937, 96040861934, 3),
-- ('Pidzg', 'Jaatzzwp', 173002606, 96031417539, 5),
-- ('Bfjkncvk', 'Ahhzjchp', 144804919, 96081494917, 2),
-- ('Dnmppn', 'Sjznkew', 106851320, 95070789869, 3),
-- ('Lgefone', 'Emmsbao', 159382853, 96050475262, 1),
-- ('Xzqmkq', 'Cuvtqvnxb', 153048498, 95080123574, 6),
-- ('Lkglzam', 'Ndnsjolvy', 173595097, 96091982499, 5),
-- ('Yttqog', 'Abaiakqllsz', 131596366, 95102776522, 2),
-- ('Iconnmoq', 'Epeefsnsmo', 163043320, 96101155712, 5),
-- ('Podsgcf', 'Desyshmgxw', 196095815, 96062216499, 3),
-- ('Yuvno', 'Sjftqtwkbap', 113823293, 95110414531, 1),
-- ('Kimqw', 'Nslgvlcsaq', 151008693, 95111862951, 2),
-- ('Fwtbseet', 'Ndnfnbyjvpd', 124048977, 96091338313, 2),
-- ('Ozqxsta', 'Xzpctth', 130546620, 96082527113, 3),
-- ('Vemgfkrb', 'Rkzvgbof', 137180529, 96042777613, 2),
-- ('Rjhdnay', 'Snbitora', 192277052, 96120729644, 3),
-- ('Ednezw', 'Fdawlohssvt', 127793660, 96090943383, 2),
-- ('Rvsyl', 'Dlucqxswy', 101422376, 96091623736, 2),
-- ('Ddmfrt', 'Eqsekejhz', 160144854, 95101222222, 3),
-- ('Jfepxch', 'Izysvdgcx', 129363923, 96033086362, 6),
-- ('Uwmea', 'Hzifktmo', 173521090, 95042777524, 5),
-- ('Ofxtgpo', 'Nqiysrs', 195266356, 95062218296, 2),
-- ('Sdjqnqc', 'Fqrnll', 124722490, 95020965295, 5),
-- ('Nzvmw', 'Hufnnxv', 163880569, 95112635929, 1),
-- ('Ogmli', 'Randly', 133480550, 96111836496, 2),
-- ('Nuaosn', 'Ivacsvpiumo', 115387142, 95012064472, 5),
-- ('Wqxswkq', 'Cxyazntnai', 104438548, 97021375213, 1),
-- ('Fybnuqb', 'Xaggxach', 189537797, 97060473428, 5),
-- ('Txqqmlfo', 'Rqhvokiia', 137140292, 97111931983, 3),
-- ('Ovxjvbs', 'Pifzyxnjcb', 149757806, 97092746929, 4),
-- ('Lmixxs', 'Thovengb', 159067697, 97072626399, 3),
-- ('Oixqg', 'Rrygxrxkfh', 118150212, 97102726569, 3),
-- ('Pnhwilk', 'Fbpeszdi', 152327934, 97042933434, 6),
-- ('Nxtzqsjw', 'Lycbmjawwm', 177511625, 97112413112, 3),
-- ('Cpfdup', 'Kcltxmkpv', 172190528, 97032668825, 2),
-- ('Btuseu', 'Lgeltkc', 144834684, 97122159752, 4),
-- ('Kbqromq', 'Dixezqkv', 159112711, 97030435623, 2),
-- ('Hwcocp', 'Krmbpbegvsu', 185859963, 97010853852, 5),
-- ('Cuuvkes', 'Htdhvtjmexf', 140106892, 97101943857, 4),
-- ('Mfdpaxcw', 'Gqjtbplyz', 109907854, 97082912721, 2),
-- ('Iwsod', 'Otqrpyu', 117244564, 97122378881, 3),
-- ('Swgfnpaq', 'Oofrsotq', 136864840, 97071174923, 3),
-- ('Xipqzeqv', 'Emuoubb', 139199093, 97031268712, 2),
-- ('Bmixfc', 'Wstnosvdkuj', 189409560, 97092994162, 4),
-- ('Odqhx', 'Riueziowo', 186713846, 97090982121, 2),
-- ('Tecwxxbj', 'Fmkjgncpmva', 198911786, 97013184775, 5),
-- ('Uausokb', 'Tgjtfiu', 119480878, 97070311699, 2),
-- ('Ilvlazam', 'Iimicn', 110058106, 97022656485, 1),
-- ('Exjlfuem', 'Adgkhuf', 117869388, 97012561225, 1),
-- ('Bjaxrni', 'Horhfrqqwnu', 176967137, 97072697124, 5),
-- ('Kyevslq', 'Olyskrh', 110411310, 97041137242, 2),
-- ('Igsoxl', 'Ayyfqu', 129841549, 97111496752, 1),
-- ('Rhmgye', 'Iyepfaesj', 123524763, 97091122416, 3),
-- ('Svdevdll', 'Mazxjndjrx', 171935247, 97081581991, 4),
-- ('Fyddqnqd', 'Jyshwxsh', 190503441, 97030243385, 4),
-- ('Kwumbffa', 'Mdnxjqoyir', 120071006, 97101891884, 5),
-- ('Nrnekxdl', 'Gjfqkkvnxu', 111111544, 97070154256, 5),
-- ('Lcixm', 'Xwsqoiwyf', 121608488, 97040933986, 2),
-- ('Vuuugfrt', 'Mmqinu', 173177440, 97082994253, 6),
-- ('Vxelpst', 'Haodqs', 166747319, 97070924789, 3),
-- ('Brfbxtn', 'Tbltqtmpy', 100313522, 97032161922, 3),
-- ('Nujuiok', 'Twswqy', 137046647, 97011543994, 2),
-- ('Tdxqqsgk', 'Jihbaawju', 116391568, 97032223172, 1),
-- ('Toddk', 'Ljizyny', 130121288, 97102049248, 5),
-- ('Qozryit', 'Prifximkyr', 191321676, 97021896499, 1),
-- ('Ovusuiq', 'Wjfcky', 133469976, 97091882765, 6),
-- ('Cekijksv', 'Dokcye', 188510062, 97111439528, 6),
-- ('Efpct', 'Kxkixdbx', 186015033, 97100924655, 5),
-- ('Iwcqq', 'Mbbfhbadv', 153744777, 97030656297, 1),
-- ('Lujxfrw', 'Ruuhep', 106740259, 97062158981, 5),
-- ('Hfkyhs', 'Quleafg', 179575260, 97011699473, 1),
-- ('Ghjwtesp', 'Seqfmnm', 131557302, 97090113262, 5),
-- ('Ereleink', 'Omfpvomq', 126782912, 97120841653, 5),
-- ('Hdmxk', 'Cwxzqnswax', 120084148, 97012394483, 2),
-- ('Edxbuu', 'Ekmnqwqdva', 197510827, 97071518583, 5),
-- ('Fhoqakq', 'Rgkmlhq', 183989693, 97011178549, 6),
-- ('Xnwzgsp', 'Crownpgeh', 194124046, 97042079545, 1),
-- ('Thfrvq', 'Kwdtkssl', 187638368, 97022857398, 1),
-- ('Catax', 'Idmyldxukd', 192469147, 97081296332, 4),
-- ('Hrrumb', 'Cmlrowrhwo', 145552368, 97093033916, 6),
-- ('Klghlc', 'Lrzhgsb', 112268428, 97011997427, 1),
-- ('Jlpccdy', 'Kxmdmfhao', 159726503, 97042296757, 4),
-- ('Rzkhi', 'Gjtimitdkx', 113877140, 97011867865, 3),
-- ('Cjecw', 'Xwabhsliev', 155191614, 97090263967, 2),
-- ('Nqeqazt', 'Jdwrbgxd', 181672606, 97111949311, 2),
-- ('Alshge', 'Rzhhvlxcbxd', 146810336, 97031895475, 6),
-- ('Bgtdoqi', 'Wyspqzvuqi', 124897797, 97041287619, 6),
-- ('Qlpvooyn', 'Kpgvswoa', 116903580, 97021067648, 1),
-- ('Nhrff', 'Tnjyeeltzai', 157242587, 97030692882, 2),
-- ('Acozw', 'Ewyhzgpq', 179629507, 97061998331, 3),
-- ('Djqipuuj', 'Wtxlbznry', 159970879, 97060354877, 6),
-- ('Cbvghmy', 'Fggtyqjtmu', 112526508, 97122573811, 5),
-- ('Ptqmih', 'Dkddnal', 198119944, 97112593137, 6),
-- ('Uxsat', 'Weldacnn', 173207249, 97012136117, 2),
-- ('Yrmrny', 'Unwbjjpdjh', 107964193, 97060847896, 3),
-- ('Rknyk', 'Ixhxclqqe', 176270887, 97121867449, 5),
-- ('Jdwzo', 'Nrrwwxyr', 179339467, 97032448474, 6),
-- ('Pdgqk', 'Uvtmzzc', 113908251, 97120861123, 1),
-- ('Wtvfio', 'Lkvederv', 171690579, 97111067138, 1),
-- ('Meghbc', 'Ebxdxezrzgb', 162361873, 97120677135, 4),
-- ('Oanffecc', 'Iqfmzjqtlrs', 177415389, 97012372638, 2),
-- ('Viywjobs', 'Efujlxn', 181195176, 97101789332, 2),
-- ('Frddiy', 'Gqfspv', 126257224, 97102464322, 1),
-- ('Wcvdr', 'Smkwlyiqdch', 132719348, 97081186622, 6),
-- ('Qytzdn', 'Dqcvdeqj', 191639518, 97030716179, 3),
-- ('Lepxca', 'Vewqmoxkj', 102497640, 97081217537, 2),
-- ('Sqorl', 'Iedvywhcog', 147795651, 97102444975, 3),
-- ('Wusfgies', 'Akrpaig', 151164576, 97031115533, 1),
-- ('Dfbubiyr', 'Pfmwaeeim', 129190168, 97030964572, 2),
-- ('Vnzcphkf', 'Bbqsvtdwl', 152262092, 97101986371, 2),
-- ('Vaungfz', 'Khbxif', 191644042, 97052715994, 2),
-- ('Mcjzsdxn', 'Qacwtj', 139193736, 97040815824, 2),
-- ('Cuxvegy', 'Tgsfxqhipbo', 132646386, 97091216124, 4),
-- ('Cpxckic', 'Fhufcz', 101004340, 97052321278, 1),
-- ('Lgwigmr', 'Oteqkbwbaa', 111608254, 97081779471, 1),
-- ('Wqlivnj', 'Komwkuc', 173365509, 97101193726, 6),
-- ('Agztqa', 'Yargkwu', 168455932, 97062582852, 4),
-- ('Hvohl', 'Wjqpop', 180476021, 97010643725, 2),
-- ('Dkoel', 'Cnzeavaacea', 138823523, 97042215547, 3),
-- ('Nydyp', 'Hgyxblh', 187337023, 97042772295, 4),
-- ('Dnkttkq', 'Hvanuuvjv', 158787454, 97012857793, 6),
-- ('Lvuvs', 'Azkywhmgc', 192095745, 97010397938, 5),
-- ('Ecqdpmzm', 'Fneikzf', 121734132, 97011235794, 4),
-- ('Htnlpwz', 'Bhnvkplpf', 151205738, 97052399499, 3),
-- ('Rngexr', 'Szzdmuszl', 105624150, 97030481129, 2),
-- ('Bkvkw', 'Flrxbl', 185622011, 97041054897, 1);

-- INSERT INTO uwagi (id_ucznia, id_nauczyciela, opis, czy_pozytywna, data_wystawienia) VALUES
-- (164, 16, 'tis in lacus eu dictum. Nullam ut imperdiet e', FALSE, '2014-04-10'::date),
-- (116, 11, 'dolor sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu dic', FALSE, '2014-04-10'::date),
-- (123, 10, ' ipsum dolor sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu dictum', FALSE, '2014-04-10'::date),
-- (167, 8, 'cus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (72, 16, 'rci.', TRUE, '2014-04-10'::date),
-- (171, 7, 'ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (150, 5, ' consectetur adipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet', FALSE, '2014-04-10'::date),
-- (148, 15, 'imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (70, 15, 'tur adipiscing elit. Praesent venenatis in lacus ', TRUE, '2014-04-10'::date),
-- (158, 10, 'onsectetur adipiscing elit. Praesent venenati', TRUE, '2014-04-10'::date),
-- (174, 8, 'tum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (154, 15, ' elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (106, 10, 'lor sit amet, consectetur adipiscing elit. Praesent venen', TRUE, '2014-04-10'::date),
-- (126, 6, ' adipiscing elit. Praesent venenatis in lac', FALSE, '2014-04-10'::date),
-- (145, 14, 'tis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (88, 6, 't imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (8, 14, 'Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (85, 15, 'm ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (133, 19, 'atis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (173, 12, 'tur adipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (136, 5, 's in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (22, 4, 'um. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (53, 3, 'r sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mo', TRUE, '2014-04-10'::date),
-- (2, 14, 'lor sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu ', FALSE, '2014-04-10'::date),
-- (41, 12, 'cus eu dictum. Nullam ut imperdiet est, in moll', FALSE, '2014-04-10'::date),
-- (12, 11, ' venenatis in lacus eu dictum. Nullam u', FALSE, '2014-04-10'::date),
-- (148, 3, 'n mollis orci.', TRUE, '2014-04-10'::date),
-- (6, 5, 's eu dictum. Nullam ut imperdiet est, in mollis o', TRUE, '2014-04-10'::date),
-- (29, 12, 'rem ipsum dolor sit amet, consectetur adipiscing elit. Prae', FALSE, '2014-04-10'::date),
-- (120, 18, 'nt venenatis in lacus eu dictum. Nulla', FALSE, '2014-04-10'::date),
-- (41, 6, 'Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (146, 9, 'ipsum dolor sit amet, consectetur adipiscing elit. ', FALSE, '2014-04-10'::date),
-- (120, 16, 'ictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (51, 15, ' orci.', TRUE, '2014-04-10'::date),
-- (25, 10, 'n mollis orci.', FALSE, '2014-04-10'::date),
-- (172, 19, 'eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (113, 3, 'mperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (48, 9, 'imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (90, 14, ' in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (129, 9, 'Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (57, 5, 'ci.', FALSE, '2014-04-10'::date),
-- (39, 5, ' est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (49, 8, 't est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (37, 19, 'm. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (171, 19, 'lis orci.', TRUE, '2014-04-10'::date),
-- (151, 4, 'onsectetur adipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdi', TRUE, '2014-04-10'::date),
-- (130, 5, ' dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (148, 10, 'g elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mol', FALSE, '2014-04-10'::date),
-- (63, 4, 'lor sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu', TRUE, '2014-04-10'::date),
-- (69, 8, 'amet, consectetur adipiscing elit. Praese', TRUE, '2014-04-10'::date),
-- (106, 10, 'in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (6, 17, ', consectetur adipiscing elit. Praesent venenatis in lacus eu dictum. Null', TRUE, '2014-04-10'::date),
-- (43, 5, 'met, consectetur adipiscing elit. Praesent ven', TRUE, '2014-04-10'::date),
-- (39, 10, 'ipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orc', TRUE, '2014-04-10'::date),
-- (1, 4, ' sit amet, consectetur adipiscing elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (11, 14, 'est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (82, 1, 'dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (4, 18, 'm ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (40, 5, 'is in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (116, 2, 'm ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (71, 9, 'llis orci.', TRUE, '2014-04-10'::date),
-- (68, 1, 'elit. Praesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (168, 7, 'raesent venenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (130, 18, 'acus eu dictum. Nullam ut imperdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (67, 12, 't, in mollis orci.', FALSE, '2014-04-10'::date),
-- (116, 4, '. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (20, 13, 'rdiet est, in mollis orci.', FALSE, '2014-04-10'::date),
-- (169, 14, 'ollis orci.', TRUE, '2014-04-10'::date),
-- (1, 17, 'enenatis in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date),
-- (62, 4, 's in lacus eu dictum. Nullam ut imperdiet est, in mollis orci.', TRUE, '2014-04-10'::date);

-- INSERT INTO plan_lekcji(id_przedmiotu, nr_lekcji, dzien_tygodnia) VALUES
-- --KLASA1a
-- (1, 1, 2),
-- (1, 2, 2),
-- (7, 3, 2),

-- (13, 1, 3),
-- (17, 2, 3),
-- (20, 3, 3),

-- (29, 1, 4),
-- (35, 2, 4),
-- (35, 3, 4),

-- (20, 1, 5),
-- (1, 2, 5),
-- (1, 3, 5),

-- (13, 1, 6),
-- (13, 2, 6),
-- (35, 3, 6),

-- --KLASA1b
-- (8, 1, 2),
-- (2, 2, 2),
-- (14, 3, 2),

-- (23, 1, 3),
-- (26, 2, 3),
-- (30, 3, 3),

-- (36, 1, 4),
-- (30, 2, 4),
-- (30, 3, 4),

-- (2, 1, 5),
-- (2, 2, 5),
-- (26, 3, 5),

-- (14, 1, 6),
-- (14, 2, 6),
-- (8, 3, 6),

-- --KLASA2a
-- (3, 1, 2),
-- (9, 2, 2),
-- (15, 3, 2),

-- (15, 1, 3),
-- (18, 2, 3),
-- (21, 3, 3),

-- (31, 1, 4),
-- (31, 2, 4),
-- (37, 3, 4),

-- (3, 1, 5),
-- (3, 2, 5),
-- (15, 3, 5),

-- (18, 1, 6),
-- (31, 2, 6),
-- (18, 3, 6),

-- --KLAS2b
-- (4, 1, 2),
-- (10, 2, 2),
-- (16, 3, 2),

-- (16, 1, 3),
-- (24, 2, 3),
-- (24, 3, 3),

-- (27, 1, 4),
-- (27, 2, 4),
-- (32, 3, 4),

-- (38, 1, 5),
-- (38, 2, 5),
-- (27, 3, 5),

-- (4, 1, 6),
-- (4, 2, 6),
-- (24, 3, 6),

-- --KLASA3a
-- (5, 1, 2),
-- (5, 2, 2),
-- (11, 3, 2),

-- (19, 1, 3),
-- (22, 2, 3),
-- (22, 3, 3),

-- (33, 1, 4),
-- (33, 2, 4),
-- (39, 3, 4),

-- (19, 1, 5),
-- (19, 2, 5),
-- (11, 3, 5),

-- (5, 1, 6),
-- (5, 2, 6),
-- (11, 3, 6),

-- --KLAS3b
-- (6, 1, 2),
-- (6, 2, 2),
-- (12, 3, 2),

-- (12, 1, 3),
-- (25, 2, 3),
-- (28, 3, 3),

-- (28, 1, 4),
-- (28, 2, 4),
-- (34, 3, 4),

-- (34, 1, 5),
-- (40, 2, 5),
-- (40, 3, 5),

-- (6, 1, 6),
-- (6, 2, 6),
-- (28, 3, 6);

-- INSERT INTO przeprowadzone_lekcje(data, temat_zajec, id_prowadzacego, id_lekcji) VALUES
-- ('2014-12-15'::date, 'xfgu hjutyfdwqdfv', 1, 1),
-- ('2014-12-15'::date, 'fdsfqeytry dfv', 2, 2),
-- ('2014-12-15'::date, 'fdsfqfdw qdfv', 3, 3),
-- ('2014-12-15'::date, 'ddg jktrtwehtjblr fqfdwqdfv', 4, 16),
-- ('2014-12-15'::date, 'fdsfq hgfh fdwqdfv', 5, 17),
-- ('2014-12-15'::date, 'fdsfqfdwqdfv', 6, 18),
-- ('2014-12-15'::date, 'pdsf nhgfju qeytry dfv', 7, 31),
-- ('2014-12-15'::date, 'kdsfqfdw qdfv', 8, 32),
-- ('2014-12-15'::date, 'fdg jklr fqfdwqdfv', 9, 33),
-- ('2014-12-15'::date, 'fkdsfq hgfh fdwqdfv', 10, 46),
-- ('2014-12-15'::date, 'kfdsffdw qdfv', 11, 47),
-- ('2014-12-15'::date, 'fpdqytry dpiopfv', 12, 48),
-- ('2014-12-15'::date, 'kfspiofdw qdfv', 13, 61),
-- ('2014-12-15'::date, 'mfdg jklr fqfdwqdfv', 14, 62),
-- ('2014-12-15'::date, 'gfdsfq hgfh fdwqdfv', 15, 63),
-- ('2014-12-15'::date, 'lsfqfqwe dw qdfv', 16, 76),
-- ('2014-12-15'::date, 'pfbvdg jklr fqfdwqdfv', 17, 77),
-- ('2014-12-15'::date, 'qfdsfq hgfh fdwqkhjkdfv', 18, 78);

-- INSERT INTO oceny_uczniow (wartosc, id_ucznia, id_przedmiotu, id_aktywnosci, tematyka) VALUES
-- (1, 11, 1, 1,'logika'),
-- (3, 13, 1, 1,'logika'),
-- (5, 16, 1, 1,'logika'),
-- (5, 26, 1, 1,'logika'),
-- (4, 27, 1, 1,'logika'),
-- (6, 40, 1, 1,'logika'),
-- (2, 44, 1, 1,'logika'),
-- (2, 53, 1, 1,'logika'),
-- (4, 53, 7, 3,'antyk'),
-- (3, 53, 7, 3,'polska'),
-- (3, 122, 7, 3,'lalka'),
-- (5, 137, 7, 3,'przediowsnie');

commit;
