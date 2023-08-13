package com.trihydro.loggerkafkaconsumer;

import com.trihydro.library.factory.KafkaFactory;
import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.tables.BsmDbTables;
import com.trihydro.library.tables.DriverAlertDbTables;
import com.trihydro.library.tables.TimDbTables;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@Import({ Utility.class, DbInteractions.class, JsonToJavaConverter.class, TimDbTables.class, BsmDbTables.class,
		SQLNullHandler.class, DriverAlertDbTables.class, EmailHelper.class, JavaMailSenderImplProvider.class,
		KafkaFactory.class })
@SpringBootApplication
@EnableConfigurationProperties(LoggerConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
