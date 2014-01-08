SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `doplan` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `doplan` ;

-- -----------------------------------------------------
-- Table `doplan`.`localidad`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `doplan`.`localidad` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `doplan`.`organizacion`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `doplan`.`organizacion` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `localidad_id` INT UNSIGNED NOT NULL,
  `nombre` VARCHAR(100) NOT NULL,
  `persona_contacto` VARCHAR(100) NOT NULL,
  `email_contacto` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_organizacion_localidad_idx` (`localidad_id` ASC),
  CONSTRAINT `fk_organizacion_localidad`
    FOREIGN KEY (`localidad_id`)
    REFERENCES `doplan`.`localidad` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
