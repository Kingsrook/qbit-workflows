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
   commit_message VARCHAR(500),
   api_name VARCHAR(100),
   api_version VARCHAR(100)
);

DROP TABLE IF EXISTS workflow_step;
CREATE TABLE workflow_step
(
   id INTEGER AUTO_INCREMENT PRIMARY KEY,
   workflow_revision_id INTEGER NOT NULL,
   workflow_step_type_name VARCHAR(100) NOT NULL,
   step_no INTEGER,
   input_values_json MEDIUMTEXT,
   description VARCHAR(500),
   summary VARCHAR(250)
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

DROP TABLE IF EXISTS workflow_run_log;
CREATE TABLE workflow_run_log
(
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   workflow_id INTEGER,
   workflow_revision_id INTEGER,
   input_data_json VARCHAR(250),
   had_error BOOLEAN,
   start_timestamp TIMESTAMP,
   end_timestamp TIMESTAMP,
   input_record_qqq_table_id INTEGER,
   input_record_id INTEGER
);

ALTER TABLE workflow_run_log ADD INDEX i_start_timestamp (start_timestamp);
ALTER TABLE workflow_run_log ADD INDEX i_workflow_revision_id (workflow_revision_id);
ALTER TABLE workflow_run_log ADD INDEX i_workflow_id (workflow_id);
ALTER TABLE workflow_run_log ADD INDEX i_input_record_id_input_record_qqq_table_id (input_record_id, input_record_qqq_table_id);
ALTER TABLE workflow_run_log ADD INDEX i_input_record_qqq_table_id (input_record_qqq_table_id);

CREATE TABLE workflow_run_log_step
(
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   workflow_run_log_id BIGINT,
   workflow_step_id INTEGER,
   seq_no INTEGER,
   input_data_json VARCHAR(250),
   output_data_json VARCHAR(250),
   start_timestamp TIMESTAMP,
   end_timestamp TIMESTAMP
);

ALTER TABLE workflow_run_log_step ADD INDEX i_workflow_run_log_id (workflow_run_log_id);

