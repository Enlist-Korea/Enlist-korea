package army.helper;

import org.springframework.boot.SpringApplication;

public class TestEnlistKoreaApplication {

	public static void main(String[] args) {
		SpringApplication.from(EnlistKoreaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
