CREATE TABLE team (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    external_id VARCHAR(255),
    CONSTRAINT uk_team_external_id UNIQUE (external_id)
);

CREATE TABLE player (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    external_id VARCHAR(255),
    CONSTRAINT uk_player_external_id UNIQUE (external_id)
);

CREATE TABLE meeting (
    id           BIGSERIAL PRIMARY KEY,
    home_team_id BIGINT NOT NULL REFERENCES team (id),
    away_team_id BIGINT NOT NULL REFERENCES team (id),
    played_on    DATE NOT NULL,
    external_id  VARCHAR(255),
    CONSTRAINT uk_meeting_external_id UNIQUE (external_id),
    CONSTRAINT ck_meeting_distinct_teams CHECK (home_team_id <> away_team_id)
);

CREATE TABLE rubber (
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL REFERENCES meeting (id),
    type       VARCHAR(20) NOT NULL,
    position   INTEGER NOT NULL,
    CONSTRAINT uk_rubber_meeting_position UNIQUE (meeting_id, position)
);

CREATE TABLE rubber_set (
    id                  BIGSERIAL PRIMARY KEY,
    rubber_id           BIGINT NOT NULL REFERENCES rubber (id),
    set_number          INTEGER NOT NULL,
    type                VARCHAR(20) NOT NULL,
    home_score          INTEGER NOT NULL,
    away_score          INTEGER NOT NULL,
    home_tiebreak_score INTEGER,
    away_tiebreak_score INTEGER,
    CONSTRAINT uk_rubber_set_rubber_number UNIQUE (rubber_id, set_number)
);

CREATE TABLE rubber_participant (
    id         BIGSERIAL PRIMARY KEY,
    rubber_id  BIGINT NOT NULL REFERENCES rubber (id),
    player_id  BIGINT NOT NULL REFERENCES player (id),
    side       VARCHAR(10) NOT NULL,
    CONSTRAINT uk_rubber_participant_rubber_side_player UNIQUE (rubber_id, side, player_id)
);
