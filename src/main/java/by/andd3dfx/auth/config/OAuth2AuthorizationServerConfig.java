package by.andd3dfx.auth.config;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import by.andd3dfx.auth.web.MainController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.endpoint.AbstractEndpoint;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${security.jwt.client-id}")
    private String clientId;

    @Value("${security.jwt.client-secret}")
    private String clientSecret;

    @Value("${security.jwt.grant-types}")
    private String[] grantTypes;

    @Value("${security.jwt.scopes}")
    private String scopes;

    @Value("${security.jwt.resource-ids}")
    private String[] resourceIds;

    @Value("${security.jwt.redirect-uris}")
    private String[] redirectUris;

    @Value("${security.jwt.cors}")
    private String[] cors;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService appUserDetailsService;

    @Autowired
    private CustomTokenEnhancer customTokenEnhancer;

    @Autowired
    private MainController mainController;

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        configurer
            .inMemory()
            .withClient(clientId)
            .secret(passwordEncoder.encode(clientSecret))
            .authorizedGrantTypes(grantTypes)
            .scopes(scopes)
            .resourceIds(resourceIds)
            .redirectUris(redirectUris);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        enhancerChain.setTokenEnhancers(Arrays.asList(customTokenEnhancer, accessTokenConverter));
        endpoints.tokenStore(tokenStore)
            .accessTokenConverter(accessTokenConverter)
            .tokenEnhancer(enhancerChain)
            .authenticationManager(authenticationManager)
            .userDetailsService(appUserDetailsService);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        OncePerRequestFilter authenticationFilter = new OncePerRequestFilter(){
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                if (HttpMethod.OPTIONS.name().equals(request.getMethod())) {
                    SecurityContextHolder.getContext().setAuthentication(optionsAuthentication(clientId));
                }
                filterChain.doFilter(request, response);
            }
        };
        oauthServer.addTokenEndpointAuthenticationFilter(new CorsFilter(corsConfigurationSource()));
        oauthServer.addTokenEndpointAuthenticationFilter(authenticationFilter);
        oauthServer.checkTokenAccess("permitAll()");
    }

    @Autowired
    private AuthorizationEndpoint authorizationEndpoint;

    @PostConstruct
    public void init() {
        authorizationEndpoint.setOAuth2RequestFactory(PrivateOAuth2RequestFactory.getOAuth2RequestFactory(mainController, authorizationEndpoint));
        authorizationEndpoint.setUserApprovalPage("forward:/oauth/custom_confirm_access");
        authorizationEndpoint.setRedirectResolver(new DefaultRedirectResolver() {
            @Override
            public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {
                String redirect = super.resolveRedirect(requestedRedirect, client);
                return (redirect != null) ? requestedRedirect : redirect;
            }

            @Override
            protected boolean redirectMatches(String requestedRedirect, String redirectUri) {
                UriComponents requestedRedirectUri = UriComponentsBuilder.fromUriString(requestedRedirect).build();
                UriComponents registeredRedirectUri = UriComponentsBuilder.fromUriString(redirectUri).build();
                return hostMatches(registeredRedirectUri.getHost(), requestedRedirectUri.getHost());
            }
        });
        authorizationEndpoint.setUserApprovalHandler(new DefaultUserApprovalHandler(){
            @Override
            public AuthorizationRequest updateAfterApproval(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
                super.updateAfterApproval(authorizationRequest, userAuthentication);
                SecurityContextHolder.getContext().setAuthentication(null);
                return authorizationRequest;
            }
        });
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList(cors));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private static class PrivateOAuth2RequestFactory extends DefaultOAuth2RequestFactory {
        public static DefaultOAuth2RequestFactory getOAuth2RequestFactory(MainController mainController, AuthorizationEndpoint authorizationEndpoint) {
            try {
                Method method = AbstractEndpoint.class.getDeclaredMethod("getClientDetailsService");
                method.setAccessible(true);
                ClientDetailsService clientDetailsService = (ClientDetailsService)method.invoke(authorizationEndpoint);
                return new PrivateOAuth2RequestFactory(mainController, clientDetailsService);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private MainController mainController;
        public PrivateOAuth2RequestFactory(MainController mainController, ClientDetailsService clientDetailsService) {
            super(clientDetailsService);
            this.mainController = mainController;
        }

        public AuthorizationRequest createAuthorizationRequest(Map<String, String> authorizationParameters) {
            AuthorizationRequest authorizationRequest = super.createAuthorizationRequest(authorizationParameters);
            mainController.setNode(authorizationParameters.get("node-name"));
            return authorizationRequest;
        }
    }

    private Authentication optionsAuthentication(String principal) {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return principal;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return principal;
            }
        };
    }
}
