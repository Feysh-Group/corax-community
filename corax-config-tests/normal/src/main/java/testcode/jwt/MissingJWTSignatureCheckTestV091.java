package testcode.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultJwtParser;
import io.jsonwebtoken.impl.TextCodec;
public class MissingJWTSignatureCheckTestV091 {

    public static final String JWT_PASSWORD = TextCodec.BASE64.encode("victory");

    private void badJwtOnParser(String accessToken) {
        Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken); // $hasMissingJwtSignatureCheck



        Jwts.parser().setSigningKey(JWT_PASSWORD).parseClaimsJwt(accessToken); // $hasMissingJwtSignatureCheck
        Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken, new JwtHandlerAdapter<Jwt<Header, Claims>>() { // $hasMissingJwtSignatureCheck
            @Override
            public Jwt<Header, Claims> onClaimsJwt(Jwt<Header, Claims> jwt) {
                return jwt;
            }
        });




        Jwts.parser().setSigningKey(JWT_PASSWORD).parsePlaintextJwt(accessToken); // $hasMissingJwtSignatureCheck
        Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken, new JwtHandlerAdapter<Jwt<Header, String>>() { // $hasMissingJwtSignatureCheck
            @Override
            public Jwt<Header, String> onPlaintextJwt(Jwt<Header, String> jwt) {
                return jwt;
            }
        });



        Jwts.parser().setSigningKey(JWT_PASSWORD).parseClaimsJws(accessToken); // !$hasMissingJwtSignatureCheck
        Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken, new JwtHandlerAdapter<Jws<Claims>>() { // !$hasMissingJwtSignatureCheck
            @Override
            public Jws<Claims> onClaimsJws(Jws<Claims> jws) {
                return jws;
            }
        });



        Jwts.parser().setSigningKey(JWT_PASSWORD).parsePlaintextJws(accessToken); // !$hasMissingJwtSignatureCheck
        Jwts.parser().setSigningKey(JWT_PASSWORD).parse(accessToken, new JwtHandlerAdapter<Jws<String>>() { // !$hasMissingJwtSignatureCheck
            @Override
            public Jws<String> onPlaintextJws(Jws<String> jws) {
                return jws;
            }
        });



        new DefaultJwtParser().setSigningKey(JWT_PASSWORD).parse(accessToken); // $hasMissingJwtSignatureCheck
        new DefaultJwtParser().setSigningKey(JWT_PASSWORD).parseClaimsJwt(accessToken); // $hasMissingJwtSignatureCheck
        new DefaultJwtParser().setSigningKey(JWT_PASSWORD).parsePlaintextJwt(accessToken); // $hasMissingJwtSignatureCheck
    }

}
