-- auth_roles
INSERT INTO `auth_role` VALUES (1,'ADMIN', 'ADMIN');
INSERT INTO `auth_role` VALUES (2,'USER', 'USER');

INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) VALUES ('employee-service','/v1/create','POST','POST_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/{employee-id}','GET','GET_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/{employee-id}','PUT','UPD_EMPLOYEE');