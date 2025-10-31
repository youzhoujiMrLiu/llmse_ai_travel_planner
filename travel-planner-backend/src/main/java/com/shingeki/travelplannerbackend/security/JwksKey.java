package com.shingeki.travelplannerbackend.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JwksKey {
    public String kty;
    public String use;
    public String alg;
    public String kid;
    @JsonProperty("n") public String modulus;   // Base64Url 编码的大整数
    @JsonProperty("e") public String exponent;  // 通常是 "AQAB"
}
