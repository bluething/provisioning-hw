-- 1) Seed the devices (no override_fragment column on device table)
INSERT INTO device (mac_address, model, username, password) VALUES
  ('AA-BB-CC-DD-EE-FF', 'DESK',       'john', 'doe'),
  ('F1-E2-D3-C4-B5-A6', 'CONFERENCE', 'sofia','red'),
  ('A1-B2-C3-D4-E5-F6', 'DESK',       'walter','white'),
  ('1A-2B-3C-4D-5E-6F', 'CONFERENCE', 'eric', 'blue');

-- 2) Seed their override fragments (only for rows that had non-null overrides)
INSERT INTO override_fragment (device_mac, type, content) VALUES
  (
    'A1-B2-C3-D4-E5-F6',
    'PROPERTIES',
    'domain=sip.anotherdomain.com\nport=5161\ntimeout=10'
  ),
  (
    '1A-2B-3C-4D-5E-6F',
    'JSON',
    '{"domain":"sip.anotherdomain.com","port":5161,"timeout":10}'
  );
