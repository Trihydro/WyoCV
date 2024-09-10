
BEGIN;
ALTER TABLE itis_code DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (1,E'Speed Limit',1,268);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (169,E'Dirt',3,6016);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (170,E'Gravel',3,5933);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (171,E'Milled',3,6017);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (172,E'Delays',3,1537);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (210,E'Stalled Vehicle',2,532);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (216,E'Avalanche',2,1308);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (218,E'Rockfall',2,1309);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (220,E'Sign Installation',2,1152);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (224,E'Look Out For Workers',3,6952);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (225,E'Mowing Operations',2,1153);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (226,E'Law Enforcement Activity',2,7041);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (347,E'45',1,12589);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (246,E'left',2,13580);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (247,E'right',2,13579);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (248,E'Reduce your speed',2,7443);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (348,E'40',1,12584);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (349,E'mph',1,8720);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (350,E'Winter Storm',1,4871);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (351,E'Fog',1,5378);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (352,E'Mudslide',1,1307);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (353,E'Fire',1,3200);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (354,E'Spaces available',1,4105);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (355,E'No parking spaces available',1,4103);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (356,E'Reduced to one lane',1,777);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (358,E'workers',1,224);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (359,E'Road Construction',1,1025);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (105,E'50',1,12594);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (106,E'55',1,12599);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (107,E'60',1,12604);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (108,E'65',1,12609);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (109,E'70',1,12614);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (110,E'75',1,12619);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (111,E'80',1,12624);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (81,E'35',1,12579);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (266,E'Exit',2,11794);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (267,E'Rest Area',2,7986);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (306,E'Keep to right',3,7425);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (307,E'Keep to left',3,7426);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (326,E'Drive carefully',2,7169);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (2,E'Accident',2,513);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (3,E'Incident',2,531);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (4,E'Hazardous material spill',2,550);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (5,E'Closed',2,770);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (6,E'Closed for the season',2,774);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (8,E'Avalanche control activities',2,1042);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (10,E'Herd of animals on roadway',2,1292);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (11,E'Landslide',2,1310);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (12,E'Wide Load',2,2050);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (13,E'No trailers',2,2568);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (14,E'Width Limit',2,2573);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (15,E'Height Limit',2,2574);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (16,E'Wildfire',2,3084);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (17,E'Major event',2,3841);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (19,E'Only a few parking spaces available',4,4104);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (21,E'No parking information available',4,4223);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (22,E'Severe weather',2,4865);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (23,E'Snow',2,4868);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (24,E'Winter storm',2,4871);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (25,E'Rain',2,4885);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (26,E'Strong winds',2,5127);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (28,E'Visibility reduced',2,5383);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (29,E'Blowing snow',2,5385);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (30,E'Black ice',2,5908);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (31,E'Wet pavement',2,5895);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (32,E'Ice',2,5906);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (33,E'Icy patches',2,5907);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (34,E'Snow drifts',2,5927);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (35,E'Dry pavement',2,6011);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (36,E'Snow tires or chains required',2,6156);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (290,E'Closed due to border state request from Colorado',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (291,E'Steep downgrade ahead',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (292,E'Sharp curve ahead',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (41,E'Drive with extreme caution',2,7170);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (42,E'Increase normal following distance',2,7173);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (43,E'Prepare to stop',3,7186);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (44,E'Stop at next safe place',2,7188);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (45,E'Only travel if absolutely necessary',2,7189);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (46,E'Falling rocks',2,12037);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (286,E'Extreme blow over risk',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (146,E'Weather emergency',2,3201);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (287,E'Closed to light, high profile vehicles',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (288,E'Advise no light trailers',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (289,E'Several Reverse curves ahead',2,NULL);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (412,E'15',1,12559);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (413,E'20',1,12564);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (414,E'25',1,12569);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (415,E'30',1,12574);
INSERT INTO itis_code (itis_code_id,description,category_id,itis_code) VALUES (426,E'10',1,12554);

COMMIT;

