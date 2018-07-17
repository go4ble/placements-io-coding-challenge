CREATE TABLE campaigns (
  id integer PRIMARY KEY,
  name varchar NOT NULL
);

CREATE TABLE line_items (
  id integer PRIMARY KEY,
  campaign_id integer REFERENCES campaigns(id),
  name varchar NOT NULL,
  booked_amount numeric NOT NULL,
  actual_amount numeric NOT NULL,
  adjustments numeric NOT NULL
);
