-- auth_roles
INSERT INTO `auth_role` VALUES (1,'ADMIN', 'ADMIN');
INSERT INTO `auth_role` VALUES (2,'USER', 'USER');

INSERT INTO auth.auth_role(id, name, authority) VALUES(1, 'ADMIN', 'CREATE_EMPLOYEE#GET_EMPLOYEE#UPDATE_EMPLOYEE');
INSERT INTO auth.auth_role(id, name, authority) VALUES(2, 'USER', 'GET_EMPLOYEE');


INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/get-all','GET','GET_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) VALUES ('employee-service','/v1/create','POST','CREATE_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/{employee-id}','GET','GET_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/{employee-id}','PUT','UPDATE_EMPLOYEE');
INSERT INTO auth_endpoint (service_name,endpoint,http_method,permission) values ('employee-service','/v1/{employee-id}','DELETE','DELETE_EMPLOYEE');