package com.example.Task.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.example.Task")
@EnableWebMvc
public class SpringConfig implements WebMvcConfigurer {

    private ApplicationContext applicationContext;

    @Autowired
    public SpringConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        Yaml yaml = new Yaml();
        File file = new File("E:\\Task\\src\\main\\resources\\application.yml");
        Map<String, Object> obj;
        try {
            obj = yaml.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> obj1 = (HashMap<String, Object>) obj.get("spring");
        Map<String, Object> obj2 = (HashMap<String, Object>) obj1.get("datasource");
        dataSource.setDriverClassName(obj2.get("driver-class-name").toString()); //  "org.postgresql.Driver");
        dataSource.setUrl(obj2.get("url").toString());             //("jdbc:postgresql://localhost:5432/mydb");
        dataSource.setUsername(obj2.get("username").toString());        //("postgres");
        dataSource.setPassword(obj2.get("password").toString());        //("123456");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
