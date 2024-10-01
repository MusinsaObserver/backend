package observer.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2ClientConfig {

	@Value("${spring.security.oauth2.client.registration.apple.client-id}")
	private String appleClientId;

	@Value("${spring.security.oauth2.client.registration.apple.client-secret}")
	private String appleClientSecret;

	@Value("${spring.security.oauth2.client.registration.apple.scope}")
	private String appleScope;

	@Value("${spring.security.oauth2.client.provider.apple.authorization-uri}")
	private String appleAuthorizationUri;

	@Value("${spring.security.oauth2.client.provider.apple.token-uri}")
	private String appleTokenUri;

	@Value("${spring.security.oauth2.client.provider.apple.jwk-set-uri}")
	private String appleJwkSetUri;

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration appleClientRegistration = ClientRegistration.withRegistrationId("apple")
			.clientId(appleClientId)
			.clientSecret(appleClientSecret)
			.scope(appleScope.split(","))
			.authorizationUri(appleAuthorizationUri)
			.tokenUri(appleTokenUri)
			.jwkSetUri(appleJwkSetUri)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("{baseUrl}/login/oauth2/code/apple")
			.clientName("Apple")
			.build();

		return new InMemoryClientRegistrationRepository(appleClientRegistration);
	}
}
