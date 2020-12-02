ALTER TABLE T_NOTIFICATION_DEF
ADD (text_msg char(1));

ALTER TABLE T_NOTIFICATION_DEF
ADD (created_for INTEGER);

ALTER TABLE T_NOTIFICATION_DEF
ADD (created_by INTEGER);

ALTER TABLE T_USER
ADD (mobile INTEGER);

ALTER TABLE T_USER
ADD (carrier INTEGER);

 CREATE TABLE T_MESSAGE_GATEWAYS
   (ID NUMBER NOT NULL PRIMARY KEY, 
	CARRIER VARCHAR2(200 BYTE), 
	COUNTRY VARCHAR2(200 BYTE), 
	GATEWAY VARCHAR2(200 BYTE)
   );


CREATE SEQUENCE T_MESSAGE_GATEWAYS_SEQ MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


ALTER TABLE T_NOTIFICATION_DEF ADD FOREIGN KEY (created_for) REFERENCES T_USER (id);
ALTER TABLE T_NOTIFICATION_DEF ADD FOREIGN KEY (created_by) REFERENCES T_USER (id);

ALTER TABLE T_USER ADD FOREIGN KEY (carrier) REFERENCES T_MESSAGE_GATEWAYS (id);

Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (1,'AirCel','India','@aircel.co.in');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (2,'Airtel','India','@airtelap.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (4,'AirFire Mobile','USA','@sms.airfiremobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (5,'Aio Wireless','USA','@mms.aiowireless.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (6,'Alaska Communications','USA','@msg.acsalaska.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (7,'Aliant Canada','Canada','@sms.wirefree.informe.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (8,'Alltel (Allied Wireless)','USA','@mms.alltelwireless.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (9,'Verizon Wireless','USA','@vzwpix.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (10,'Ameritech','USA','@paging.acswireless.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (11,'Andhra Pradesh AirTel','India','@airtelap.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (12,'Andhra Pradesh Idea','India','@ideacellular.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (13,'Assurance Wireless','USA','@vmobl.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (14,'ATT Mobility','USA','@mms.att.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (15,'ATT Enterprise Paging','USA','@page.att.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (16,'Beeline','Russia','@sms.beemail.ru');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (17,'Bell Mobility','Canada','@txt.bell.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (18,'BellSouth','USA','@bellsouth.cl');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (19,'Bluegrass Cellular','USA','@mms.myblueworks.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (20,'Bluesky Communications','USA Samoa','@psms.bluesky.as');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (21,'Boost mobile','USA','@myboostmobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (22,'Bouygues Telecom','France','@mms.bouyguestelecom.fr');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (23,'Box Internet Services','Switzerland','@mms.boxis.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (24,'SFR','France','@sfr.fr');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (25,'CellCom','USA','@cellcom.quiktxt.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (26,'Cellular South','USA','@csouth1.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (27,'Centennial Wireless','USA','@cwemail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (28,'Chariton Valley Wireless','USA','@sms.cvalley.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (29,'Chat Mobility','USA','@mail.msgsender.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (30,'Chennai Skycell / Airtel','India','@airtelchennai.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (31,'Chennai RPG Cellular','India','@rpgmail.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (32,'China Mobile','China','@139.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (33,'Cincinnati Bell','USA','@mms.gocbw.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (34,'Claro','Brazil','@clarotorpedo.com.br');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (35,'Claro','Nicaragua','@ideasclaro-ca.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (36,'Claro','Puerto Rico','@vtexto.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (37,'Cleartalk','USA','@sms.cleartalk.us');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (38,'Claro','Colombia','@iclaro.com.co');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (39,'Cricket','USA','@mms.mycricket.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (40,'C Spire Wireless','USA','@cspire1.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (41,'Accessyou','Hong Kong','@messaging.accessyou.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (42,'CSL','Hong Kong','@mgw.mmsc1.hkcsl.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (43,'CTI Móvil (Now Claro)','Argentina','@sms.ctimovil.com.ar');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (44,'Delhi Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (45,'Digicel','Dominica','@digitextdm.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (46,'DTC','USA','@sms.advantagecell.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (47,'E-Plus','Germany','@smsmail.eplus.de');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (48,'Edge Wireless','USA','@sms.edgewireless.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (49,'Element Mobile','USA','@SMS.elementmobile.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (50,'Emtel','Mauritius','@emtelworld.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (51,'Esendex','International','@echoemail.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (52,'Esendex','Spain','@esendex.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (53,'Fido','Canada','@sms.fido.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (54,'Firmensms','Austria','@subdomain.firmensms.at');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (55,'Freebie SMS','Europe','@smssturen.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (56,'General Communications Inc.','USA','@mobile.gci.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (57,'Globalstar','International','@msg.globalstarusa.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (58,'Globul','Bulgaria','@sms.globul.bg');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (59,'Goa Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (60,'Goa Idea Cellular','India','@ideacellular.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (61,'Golden State Cellular','USA','@gscsms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (62,'Greatcall','USA','@vtxt.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (63,'Gujarat Idea Cellular','India','@ideacellular.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (64,'Gujarat Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (65,'Gujarat Celforce / Fascel','India','@celforce.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (66,'Guyana TT','Guyana','@sms.cellinkgy.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (67,'Haryana Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (68,'Haryana Escotel','India','@escotelmobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (69,'Hawaiian Telcom','Hawaii USA','@hawaii.sprintpcs.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (70,'HSL Mobile','UK','@sms.haysystems.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (71,'Helio','South Korea','@myhelio.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (72,'Himachai Pradesh Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (73,'ICE','Costa Rica','@sms.ice.cr');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (74,'I-wireless(T-Mobile)','USA','@iwspcs.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (75,'I-wireless(Sprint PCS)','USA','@iwirelesshometext.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (76,'Kajeet','USA','@mobile.kajeet.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (77,'Karnataka Airtel','India','@airtelkk.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (78,'Kerala Airtel','India','@airtelkerala.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (79,'Kerala Escotel','India','@escotelmobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (80,'Kolkata Airtel','India','@airtelkol.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (81,'Koodo Mobile','Canada','@msg.telus.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (82,'LongLines','USA','@text.longlines.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (83,'Lynx Mobility','Canada','@sms.lynxmobility.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (84,'M1','Singapore','@m1.com.sg');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (85,'Madhya Pradesh Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (86,'Maharashtra Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (87,'Maharashtra Idea','India','@ideacellular.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (88,'Más Móvil','Panama','@cwmovil.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (89,'Mediaburst','UK','@sms.mediaburst.co.uk');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (90,'MetroPCS','USA','@mymetropcs.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (91,'Mobiltel','Bulgaria','@sms.mtel.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (92,'Mobitel','Sri Lanka','@sms.mobitel.lk');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (93,'Movistar','Argentina','@sms.movistar.net.ar');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (94,'Movistar','Colombia','@movistar.com.co');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (95,'Movistar','Spain','@movistar.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (96,'Movistar','Latin America','@movimensaje.com.ar');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (97,'Movistar','Uruguay','@sms.movistar.com.uy');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (98,'MTN','South Africa','@sms.co.za');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (99,'MTS Mobility','Canada','@text.mtsmobility.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (100,'Mumbai Airtel','India','@airtelmail.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (101,'My-Cool-SMS','UK','@my-cool-sms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (102,'Nextech','USA','@sms.ntwls.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (103,'Nextel','Mexico','@msgnextel.com.mx');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (104,'Nextel','Argentina','@nextel.net.ar');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (105,'O2','Germany','@o2online.de');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (106,'OgVodafone','Iceland','@sms.is');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (107,'Orange','Netherlands','@sms.orange.nl');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (108,'Orange','UK','@orange.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (109,'Page Plus Cellular (Verizon)','USA','@vzwpix.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (110,'Pioneer Cellular','USA','@zsend.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (111,'Personal','Argentina','@alertas.personal.com.ar');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (112,'Plus','Polan','@text.plusgsm.pl');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (113,'Pocket Wireless','USA','@sms.pocket.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (114,'PC Telecom','Canada','@mobiletxt.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (115,'Polkomtel','Poland','@text.plusgsm.pl');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (116,'Qwest Wireless','USA','@qwestmp.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (117,'Red Pocket Mobile','USA','@vtext.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (118,'Rogers Wireless','Canada-USA','@mms.rogers.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (119,'SaskTel','Canada','@pcs.sasktelmobility.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (120,'Sendega','Norway','@sendega.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (121,'Setar Mobile email (Aruba)','Aruba','@mas.aw');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (122,'Síminn','Iceland','@box.is');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (123,'Simple Mobile','USA','@smtext.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (124,'SMS Broadcast','Australia','@send.smsbroadcast.com.au');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (125,'SMS Central','Australia','@sms.smscentral.com.au');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (126,'SMSPUP','Australia','@smspup.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (127,'Southernlinc','USA','@page.southernlinc.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (128,'South Central Communications','USA','@rinasms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (129,'Spikko','Israel','@SpikkoSMS.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (130,'Sprint','USA','@pm.sprint.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (131,'Sunrise Communications','Switzerland','@gsm.sunrise.ch');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (132,'Syringa Wireless','USA','@rinasms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (133,'TeletopiaSMS','Norway','@sms.teletopiasms.no');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (134,'T-Mobile','USA','@tmomail.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (135,'T-Mobile (Optus Zoo)','Australia','@optusmobile.com.au');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (136,'T-Mobile','Austria','@sms.t-mobile.at');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (137,'T-Mobile','Croatia','@sms.t-mobile.hr');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (138,'T-Mobile','Germany','@t-mobile-sms.de');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (139,'T-Mobile','Netherlands','@gin.nl');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (140,'Tamil Nadu Airtel','India','@airtelmobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (141,'Tamil Nadu Aircel','India','@airsms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (142,'Tele2','Sweden','@sms.tele2.se');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (143,'Telecom','New Zealand','@etxt.co.nz');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (144,'Teleflip','USA','@teleflip.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (145,'Telstra','Australia','@sms.tim.telstra.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (146,'Telus Mobility','Canada-USA','@mms.telusmobility.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (147,'TellusTalk','Europe','@esms.nu');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (148,'Tigo (Formerly Ola)','Colombia','@sms.tigo.com.co');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (149,'TIM','Italy','@timnet.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (150,'Ting','USA','@message.ting.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (151,'TracFone (prepaid)','USA','@mmst5.tracfone.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (152,'Txtlocal','UK','@txtlocal.co.uk');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (153,'Telcel','Mexico','@itelcel.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (154,'Unicel','USA','@utext.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (155,'UniMóvil Corporation','UK','@viawebsms.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (156,'Union Wireless','USA','@union-tel.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (157,'US Cellular','USA','@mms.uscc.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (158,'USA Mobility','USA','@usamobility.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (159,'UTBox','Australia','@sms.utbox.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (160,'Uttar Pradesh West Escotel','India','@escotelmobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (161,'Verizon Wireless','USA','@vzwpix.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (162,'Viaero','USA','@mmsviaero.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (163,'Vivo','Brazil','@torpedoemail.com.br');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (164,'Virgin Mobile','Canada','@vmobile.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (165,'Virgin Mobile','USA','@vmpix.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (166,'Virgin Mobile','UK','@vxtras.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (167,'Vodacom','South Africa','@voda.co.za');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (168,'Vodafone','Germany','@vodafone-sms.de');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (169,'Vodafone','Italy','@sms.vodafone.it');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (170,'Vodafone','Portugal','@sms.vodafone.');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (171,'Vodafone','Spain','@vodafone.es');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (172,'Vodafone','New Zealand','@mtxt.co.nz');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (173,'Voyager Mobile','USA','@text.voyagermobile.com');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (174,'West Central Wireless','USA','@sms.wcc.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (175,'Wind Mobile','Canada','@txt.windmobile.ca');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (176,'XIT Communications','USA','@sms.xit.net');
Insert into T_MESSAGE_GATEWAYS (ID,CARRIER,COUNTRY,GATEWAY) values (177,'Straigt Talk','USA','@vtext.com');


exit;


