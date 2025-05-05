DROP TABLE IF EXISTS workflow;
CREATE TABLE workflow
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   name VARCHAR(100) NOT NULL,
   workflow_type_name VARCHAR(100) NOT NULL,
   current_workflow_revision_id INTEGER
);

DROP TABLE IF EXISTS workflow_revision;
CREATE TABLE workflow_revision
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   create_date TIMESTAMP DEFAULT now(),
   modify_date TIMESTAMP DEFAULT now(),
   workflow_id INTEGER,
   version_no INTEGER,
   start_step_no INTEGER,
   author VARCHAR(500),
   commit_message VARCHAR(500)
);

DROP TABLE IF EXISTS workflow_step;
CREATE TABLE workflow_step
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   workflow_revision_id INTEGER NOT NULL,
   workflow_step_type_name VARCHAR(100) NOT NULL,
   step_no INTEGER,
   input_values_json MEDIUMTEXT,
   description VARCHAR(500)
);

DROP TABLE IF EXISTS workflow_link;
CREATE TABLE workflow_link
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   workflow_revision_id INTEGER NOT NULL,
   from_step_no INTEGER,
   to_step_no INTEGER,
   condition_value VARCHAR(20)
);
