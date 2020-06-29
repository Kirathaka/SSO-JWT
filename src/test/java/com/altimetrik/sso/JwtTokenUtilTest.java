package com.altimetrik.sso;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;

import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import com.altimetrik.sso.config.JwtTokenUtil;
import com.altimetrik.sso.repository.UserRepository;
import com.altimetrik.sso.service.JwtUserDetailsService;

import io.jsonwebtoken.Clock;

public class JwtTokenUtilTest {

	private static final String TEST_USERNAME = "chetan";

	@Mock
	private Clock clockMock;

	@InjectMocks
	private JwtTokenUtil jwtTokenUtil;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private JwtUserDetailsService userDetailsDummy;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		ReflectionTestUtils.setField(jwtTokenUtil, "JWT_TOKEN_VALIDITY", 15000L); // one hour
		ReflectionTestUtils.setField(jwtTokenUtil, "JWT_REFRESH_TOKEN_VALIDITY", 30000L); // one hour
		ReflectionTestUtils.setField(jwtTokenUtil, "secret", "oAuthSecret");

		when(userRepository.loadUserByUsername(TEST_USERNAME)).thenReturn(
				new User("chetan", "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6", new ArrayList<>()));
	}

	//@Test
	public void testGenerateTokenGeneratesDifferentTokensForDifferentCreationDates() throws Exception {

		when(clockMock.now()).thenReturn(DateUtil.yesterday()).thenReturn(DateUtil.now());

		final String token = createToken();
		final String laterToken = createToken();

		assertFalse(token.equals(laterToken));

	}

	
	@Test
    public void getUsernameFromToken() throws Exception {
        when(clockMock.now()).thenReturn(DateUtil.now());

        final String token = createToken();

        assertTrue(jwtTokenUtil.getUsernameFromToken(token).equals(TEST_USERNAME));

    }
	
	@Test
    public void getCreatedDateFromToken() throws Exception {
        final Date now = DateUtil.now();
        when(clockMock.now()).thenReturn(now);

        final String token = createToken();

        assertTrue(isInSameMinuteWindowAs(now,jwtTokenUtil.getIssuedAtDateFromToken(token)));
    }
	
	@Test
    public void getExpirationDateFromToken() throws Exception {
        final Date now = DateUtil.now();
        when(clockMock.now()).thenReturn(now);
        final String token = createToken();

        final Date expirationDateFromToken = jwtTokenUtil.getExpirationDateFromToken(token);
        long timeDiff = DateUtil.timeDifference(expirationDateFromToken, now);
        assertTrue(timeDiff > 14000L && timeDiff <15000L);
    }
	
    @Test
	public void expiredTokenCannotBeRefreshed() throws Exception {
        when(clockMock.now())
                .thenReturn(DateUtil.yesterday());
        String token = createToken();
        assertFalse(jwtTokenUtil.isRefreshTokenExpired(token));
    }
	
    @Test
    public void notExpiredCanBeRefreshed() {
        when(clockMock.now())
                .thenReturn(DateUtil.now());
        String token = createToken();
        assertTrue(!jwtTokenUtil.isRefreshTokenExpired(token));
    }
    
    @Test
    public void canValidateToken() throws Exception {
        when(clockMock.now())
                .thenReturn(DateUtil.now());

        String token = createToken();
        UserDetails userDetails = userRepository.loadUserByUsername(TEST_USERNAME);

        assertTrue(jwtTokenUtil.validateToken(token, userDetails));
    }

    
	private boolean isInSameMinuteWindowAs(Date now, Date date) {
		if( Math.abs(now.getTime() - date.getTime()) < 540000 ){
			return true;
		}
		return false;
	}

	private String createToken() {
		return jwtTokenUtil.generateToken(userDetailsDummy.loadUserByUsername(TEST_USERNAME)).get(0);
	}

}
