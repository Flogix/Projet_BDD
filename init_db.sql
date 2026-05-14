-- Initialisation de la base de données ArtConnect Pro
CREATE DATABASE IF NOT EXISTS artconnect;
USE artconnect;

-- 1. TABLES
CREATE TABLE IF NOT EXISTS artiste (
    id_artiste INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    bio TEXT,
    annee_naissance INT,
    email VARCHAR(100),
    phone VARCHAR(20),
    ville VARCHAR(100),
    discipline VARCHAR(100),
    website VARCHAR(150),
    social_media VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS oeuvre (
    id_oeuvre INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(150) NOT NULL,
    annee_creation INT,
    type VARCHAR(50),
    medium VARCHAR(50),
    dimensions VARCHAR(50),
    description TEXT,
    prix DECIMAL(10, 2),
    statut VARCHAR(20) DEFAULT 'Disponible',
    id_artiste INT,
    FOREIGN KEY (id_artiste) REFERENCES artiste(id_artiste) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS galerie (
    id_galerie INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    adresse VARCHAR(200),
    nom_proprietaire VARCHAR(100),
    heures_ouverture VARCHAR(100),
    telephone VARCHAR(20),
    note DECIMAL(3, 2) DEFAULT 0.0,
    site_web VARCHAR(150)
);

CREATE TABLE IF NOT EXISTS exposition (
    id_exposition INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(150) NOT NULL,
    date_debut DATE,
    date_fin DATE,
    description TEXT,
    nom_curateur VARCHAR(100),
    theme VARCHAR(100),
    id_galerie INT,
    FOREIGN KEY (id_galerie) REFERENCES galerie(id_galerie) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS atelier (
    id_atelier INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(150) NOT NULL,
    date_atelier DATETIME,
    duree_minutes INT,
    max_participants INT,
    prix DECIMAL(10, 2),
    lieu VARCHAR(150),
    description TEXT,
    niveau VARCHAR(50),
    id_artiste INT,
    FOREIGN KEY (id_artiste) REFERENCES artiste(id_artiste) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS visiteur (
    id_visiteur INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    annee_naissance INT,
    telephone VARCHAR(20),
    ville VARCHAR(100),
    type_abonnement VARCHAR(20) DEFAULT 'gratuit'
);

-- 2. INDEXES
-- Indexing gallery name and workshop date for faster searches
CREATE INDEX idx_galerie_nom ON galerie(nom);
CREATE INDEX idx_atelier_date ON atelier(date_atelier);

-- 3. VIEWS
-- View to get detailed exhibition info easily
CREATE OR REPLACE VIEW v_exposition_details AS
SELECT 
    e.id_exposition,
    e.titre AS titre_exposition,
    e.date_debut,
    e.date_fin,
    e.theme,
    g.id_galerie,
    g.nom AS nom_galerie,
    g.adresse AS adresse_galerie
FROM exposition e
JOIN galerie g ON e.id_galerie = g.id_galerie;

-- 4. TRIGGERS
-- Trigger to ensure rating is between 0 and 5 before inserting a gallery
DELIMITER //
CREATE TRIGGER trg_before_insert_galerie
BEFORE INSERT ON galerie
FOR EACH ROW
BEGIN
    IF NEW.note < 0.0 THEN
        SET NEW.note = 0.0;
    ELSEIF NEW.note > 5.0 THEN
        SET NEW.note = 5.0;
    END IF;
END;
//
DELIMITER ;

-- 5. FUNCTIONS
-- Function to calculate maximum potential revenue of a workshop
DELIMITER //
CREATE FUNCTION get_max_revenu_atelier(a_id INT) 
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE max_rev DECIMAL(10,2);
    SELECT (max_participants * prix) INTO max_rev 
    FROM atelier 
    WHERE id_atelier = a_id;
    
    RETURN IFNULL(max_rev, 0.0);
END;
//
DELIMITER ;

-- INSERT DUMMY DATA FOR DEMONSTRATION
INSERT INTO galerie (nom, adresse, note) VALUES 
('Lumina Art Gallery', '123 Art Avenue, Paris', 4.8),
('Modern Vibes', '45 Creative Blvd, Lyon', 4.5);

INSERT INTO exposition (titre, date_debut, date_fin, theme, id_galerie) VALUES 
('Spring Awakening', '2026-05-01', '2026-05-30', 'Nature', 1),
('Abstract Realities', '2026-06-15', '2026-07-15', 'Abstract', 2);

INSERT INTO artiste (nom, prenom, bio, ville, discipline) VALUES 
('Dubois', 'Alice', 'Contemporary painter', 'Paris', 'Peinture'),
('Chagal', 'Marc', 'Sculptor and thinker', 'Lyon', 'Sculpture');

INSERT INTO oeuvre (titre, type, prix, statut, id_artiste) VALUES 
('Le Printemps', 'Peinture', 1200.00, 'Disponible', 1),
('Le Penseur', 'Sculpture', 3400.00, 'Disponible', 2);

INSERT INTO atelier (titre, date_atelier, duree_minutes, max_participants, prix, lieu, id_artiste) VALUES 
('Intro to Oil Painting', '2026-05-20 14:00:00', 120, 15, 45.00, 'Studio A', 1),
('Advanced Sculpting', '2026-06-10 10:00:00', 180, 10, 80.00, 'Workshop B', 2);
