-- create tables
CREATE TABLE device (
  mac_address VARCHAR(17) PRIMARY KEY,
  model        VARCHAR(20) NOT NULL,
  username    VARCHAR(100) NOT NULL,
  password    VARCHAR(100) NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE override_fragment (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  device_mac  VARCHAR(17) NOT NULL,
  type        VARCHAR(20)    NOT NULL,
  content     CLOB           NOT NULL,
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_device
    FOREIGN KEY (device_mac)
    REFERENCES device(mac_address)
    ON DELETE CASCADE
);
