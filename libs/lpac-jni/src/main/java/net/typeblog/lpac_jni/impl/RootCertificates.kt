package net.typeblog.lpac_jni.impl

import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

const val DEFAULT_PKID_GSMA_RSP2_ROOT_CI1 = "81370f5125d0b1d408d4c3b232e6d25e795bebfb"

// SGP.28 v1.0, eSIM CI Registration Criteria (Page 5 of 9, 2019-10-24)
// https://www.gsma.com/newsroom/wp-content/uploads/SGP.28-v1.0.pdf#page=5
// FS.27 v2.0, Security Guidelines for UICC Profiles (Page 25 of 27, 2024-01-30)
// https://www.gsma.com/solutions-and-impact/technologies/security/wp-content/uploads/2024/01/FS.27-Security-Guidelines-for-UICC-Credentials-v2.0-FINAL-23-July.pdf#page=25

// List of GSMA Live CIs
// https://www.gsma.com/solutions-and-impact/technologies/esim/gsma-root-ci/
val PKID_GSMA_LIVE_CI = setOf(
    // GSMA RSP2 Root CI1 (SGP.22 v2+v3, CA: DigiCert)
    // https://euicc-manual.osmocom.org/docs/pki/ci/files/81370f.txt
    DEFAULT_PKID_GSMA_RSP2_ROOT_CI1,
    // OISITE GSMA CI G1 (SGP.22 v2+v3, CA: WISeKey)
    // https://euicc-manual.osmocom.org/docs/pki/ci/files/4c2796.txt
    "4c27967ad20c14b391e9601e41e604ad57c0222f",
)

// SGP.26 v3.0, 2023-12-01
// https://www.gsma.com/solutions-and-impact/technologies/esim/wp-content/uploads/2023/12/SGP.26-v3.0.pdf
val PKID_GSMA_TEST_CI = setOf(
    // Test CI (SGP.26, NIST P256)
    // https://euicc-manual.osmocom.org/docs/pki/ci/files/34eecf.txt
    "34eecf13156518d48d30bdf06853404d115f955d",
    // Test CI (SGP.26, BRP P256r1)
    // https://euicc-manual.osmocom.org/docs/pki/ci/files/2209f6.txt
    "2209f61cd9ec5c9c854e787341ff83ecf9776a5b",
)

private fun getCertificate(keyId: String): Certificate? =
    KNOWN_CI_CERTS[keyId]?.toByteArray()?.let { cert ->
        ByteArrayInputStream(cert).use { stream ->
            val cf = CertificateFactory.getInstance("X.509")
            cf.generateCertificate(stream)
        }
    }

internal fun keyIdToKeystore(keyIds: Array<String>): KeyStore {
    val ret = KeyStore.getInstance(KeyStore.getDefaultType())
    ret.load(null, null)
    keyIds.forEach {
        getCertificate(it)?.let { cert ->
            ret.setCertificateEntry(it, cert)
        }
    }

    // At the very least, we should always have GSMA ROOT CI1 trusted
    // many servers supporting custom roots are served with GSMA ROOT CI1 for TLS
    if (!ret.isCertificateEntry(DEFAULT_PKID_GSMA_RSP2_ROOT_CI1)) {
        getCertificate(DEFAULT_PKID_GSMA_RSP2_ROOT_CI1)?.let { cert ->
            ret.setCertificateEntry(DEFAULT_PKID_GSMA_RSP2_ROOT_CI1, cert)
        }
    }

    return ret
}

// ref: <https://euicc-manual.osmocom.org/docs/pki/ci/>
internal val KNOWN_CI_CERTS = hashMapOf(
    // GSM Association - RSP2 Root CI1 (CA: DigiCert)
    // Specs: SGP.21 and SGP.22 version 2 and version 3
    "81370f5125d0b1d408d4c3b232e6d25e795bebfb" to """
        -----BEGIN CERTIFICATE-----
        MIICSTCCAe+gAwIBAgIQbmhWeneg7nyF7hg5Y9+qejAKBggqhkjOPQQDAjBEMRgw
        FgYDVQQKEw9HU00gQXNzb2NpYXRpb24xKDAmBgNVBAMTH0dTTSBBc3NvY2lhdGlv
        biAtIFJTUDIgUm9vdCBDSTEwIBcNMTcwMjIyMDAwMDAwWhgPMjA1MjAyMjEyMzU5
        NTlaMEQxGDAWBgNVBAoTD0dTTSBBc3NvY2lhdGlvbjEoMCYGA1UEAxMfR1NNIEFz
        c29jaWF0aW9uIC0gUlNQMiBSb290IENJMTBZMBMGByqGSM49AgEGCCqGSM49AwEH
        A0IABJ1qutL0HCMX52GJ6/jeibsAqZfULWj/X10p/Min6seZN+hf5llovbCNuB2n
        unLz+O8UD0SUCBUVo8e6n9X1TuajgcAwgb0wDgYDVR0PAQH/BAQDAgEGMA8GA1Ud
        EwEB/wQFMAMBAf8wEwYDVR0RBAwwCogIKwYBBAGC6WAwFwYDVR0gAQH/BA0wCzAJ
        BgdngRIBAgEAME0GA1UdHwRGMEQwQqBAoD6GPGh0dHA6Ly9nc21hLWNybC5zeW1h
        dXRoLmNvbS9vZmZsaW5lY2EvZ3NtYS1yc3AyLXJvb3QtY2kxLmNybDAdBgNVHQ4E
        FgQUgTcPUSXQsdQI1MOyMubSXnlb6/swCgYIKoZIzj0EAwIDSAAwRQIgIJdYsOMF
        WziPK7l8nh5mu0qiRiVf25oa9ullG/OIASwCIQDqCmDrYf+GziHXBOiwJwnBaeBO
        aFsiLzIEOaUuZwdNUw==
        -----END CERTIFICATE-----
    """.trimIndent(),
    // OISITE GSMA CI G1 (CA: WISeKey)
    // Specs: SGP.21 and SGP.22 version 2 and version 3
    "4c27967ad20c14b391e9601e41e604ad57c0222f" to """
        -----BEGIN CERTIFICATE-----
        MIIB9zCCAZ2gAwIBAgIUSpBSCCDYPOEG/IFHUCKpZ2pIAQMwCgYIKoZIzj0EAwIw
        QzELMAkGA1UEBhMCQ0gxGTAXBgNVBAoMEE9JU1RFIEZvdW5kYXRpb24xGTAXBgNV
        BAMMEE9JU1RFIEdTTUEgQ0kgRzEwIBcNMjQwMTE2MjMxNzM5WhgPMjA1OTAxMDcy
        MzE3MzhaMEMxCzAJBgNVBAYTAkNIMRkwFwYDVQQKDBBPSVNURSBGb3VuZGF0aW9u
        MRkwFwYDVQQDDBBPSVNURSBHU01BIENJIEcxMFkwEwYHKoZIzj0CAQYIKoZIzj0D
        AQcDQgAEvZ3s3PFC4NgrCcCMmHJ6DJ66uzAHuLcvjJnOn+TtBNThS7YHLDyHCa2v
        7D+zTP+XTtgqgcLoB56Gha9EQQQ4xKNtMGswDwYDVR0TAQH/BAUwAwEB/zAQBgNV
        HREECTAHiAVghXQFDjAXBgNVHSABAf8EDTALMAkGB2eBEgECAQAwHQYDVR0OBBYE
        FEwnlnrSDBSzkelgHkHmBK1XwCIvMA4GA1UdDwEB/wQEAwIBBjAKBggqhkjOPQQD
        AgNIADBFAiBVcywTj017jKpAQ+gwy4MqK2hQvzve6lkvQkgSP6ykHwIhAI0KFwCD
        jnPbmcJsG41hUrWNlf+IcrMvFuYii0DasBNi
        -----END CERTIFICATE-----
    """.trimIndent(),
    // Symantec RSP Test Root CA (CA: DigiCert)
    "665a1433d67c1a2c5db8b52c967f10a057ba5cb2" to """
        -----BEGIN CERTIFICATE-----
        MIICkDCCAjagAwIBAgIQPfCO5OYL+cdbbx2ETDO7DDAKBggqhkjOPQQDAjBoMR0w
        GwYDVQQKExRTeW1hbnRlYyBDb3Jwb3JhdGlvbjFHMEUGA1UEAxM+U3ltYW50ZWMg
        Q29ycG9yYXRpb24gUlNQIFRlc3QgUm9vdCBDQSAtIEZvciBUZXN0IFB1cnBvc2Vz
        IE9ubHkwHhcNMTcwNzExMDAwMDAwWhcNNDkxMjMxMjM1OTU5WjBoMR0wGwYDVQQK
        ExRTeW1hbnRlYyBDb3Jwb3JhdGlvbjFHMEUGA1UEAxM+U3ltYW50ZWMgQ29ycG9y
        YXRpb24gUlNQIFRlc3QgUm9vdCBDQSAtIEZvciBUZXN0IFB1cnBvc2VzIE9ubHkw
        WTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQlbEYt9PTmdWcaX5WC68SYTFyZcbBN
        vFpJW6bZQpERlMIAuzEpgscbTDccHtNpDqJwMqZXCO7ebCmRLyI6jqe3o4HBMIG+
        MA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH/MBcGA1UdIAEB/wQNMAsw
        CQYHZ4ESAQIBADBPBgNVHR8ESDBGMESgQqBAhj5odHRwOi8vcGtpLWNybC5zeW1h
        dXRoLmNvbS9TeW1hbnRlY1JTUFRlc3RSb290Q0EvTGF0ZXN0Q1JMLmNybDASBgNV
        HREECzAJiAcrBgEEAYMJMB0GA1UdDgQWBBRmWhQz1nwaLF24tSyWfxCgV7pcsjAK
        BggqhkjOPQQDAgNIADBFAiAQ1quTqcexvDnKvmAkqoQP09QMXAXxlCyma82NtrYq
        UQIhAP/W6pRamBGhSliV+EancgbZj+VoOkKdj0o7sP/cKdhZ
        -----END CERTIFICATE-----
    """.trimIndent(),
    // GSMA Test CI
    // Specs: SGP.26 v1
    "f54172bdf98a95d65cbeb88a38a1c11d800a85c3" to """
        -----BEGIN CERTIFICATE-----
        MIICVjCCAfugAwIBAgIJALh086v6bETTMAoGCCqGSM49BAMCMEkxFTATBgNVBAMM
        DEdTTUEgVGVzdCBDSTERMA8GA1UECwwIVEVTVENFUlQxEDAOBgNVBAoMB1JTUFRF
        U1QxCzAJBgNVBAYTAklUMCAXDTE3MDIwMTE1Mzk0MloYDzIwNTIwMjAxMTUzOTQy
        WjBJMRUwEwYDVQQDDAxHU01BIFRlc3QgQ0kxETAPBgNVBAsMCFRFU1RDRVJUMRAw
        DgYDVQQKDAdSU1BURVNUMQswCQYDVQQGEwJJVDBZMBMGByqGSM49AgEGCCqGSM49
        AwEHA0IABJQGV6Zz3CiPidUuqKR3BJknkfnDSwA25jPi0MupRU1l2zLrF5gXmdLy
        Q4juK5XBCUVGyXkBzq66llCRmi4g0imjgckwgcYwHQYDVR0OBBYEFPVBcr35ipXW
        XL64ijihwR2ACoXDMA8GA1UdEwEB/wQFMAMBAf8wFwYDVR0gAQH/BA0wCzAJBgdn
        gRIBAgEAMA4GA1UdDwEB/wQEAwIBBjAOBgNVHREEBzAFiAOINwEwWwYDVR0fBFQw
        UjAnoCWgI4YhaHR0cDovL2NpLnRlc3QuZ3NtYS5jb20vQ1JMLUEuY3JsMCegJaAj
        hiFodHRwOi8vY2kudGVzdC5nc21hLmNvbS9DUkwtQi5jcmwwCgYIKoZIzj0EAwID
        SQAwRgIhAJHyUclxU7nPhTeadItXKkloUkVWxH8z62l7VZEswPLSAiEA3OSec/NJ
        7NBZEO+d9raahnq/OJ3Ia4QRtN/hlpFQ9fk=
        -----END CERTIFICATE-----
    """.trimIndent(),
    "c0bc70ba36929d43b467ff57570530e57ab8fcd8" to """
        -----BEGIN CERTIFICATE-----
        MIICVTCCAfygAwIBAgIJALh086v6bETTMAoGCCqGSM49BAMCMEkxFTATBgNVBAMM
        DEdTTUEgVGVzdCBDSTERMA8GA1UECwwIVEVTVENFUlQxEDAOBgNVBAoMB1JTUFRF
        U1QxCzAJBgNVBAYTAklUMCAXDTE3MDQxOTEwMzQzOFoYDzIwNTIwNDE4MTAzNDM4
        WjBJMRUwEwYDVQQDDAxHU01BIFRlc3QgQ0kxETAPBgNVBAsMCFRFU1RDRVJUMRAw
        DgYDVQQKDAdSU1BURVNUMQswCQYDVQQGEwJJVDBaMBQGByqGSM49AgEGCSskAwMC
        CAEBBwNCAAQnh7TVbtgkqea+BOGIf2uYuv64x2P1KDf3ARPpNk7dvhObwMfefhaP
        HlzQwYU0/FBk5k9obcUzJ8p/Hc6oWimUo4HJMIHGMB0GA1UdDgQWBBTAvHC6NpKd
        Q7Rn/1dXBTDlerj82DAPBgNVHRMBAf8EBTADAQH/MBcGA1UdIAEB/wQNMAswCQYH
        Z4ESAQIBADAOBgNVHQ8BAf8EBAMCAQYwDgYDVR0RBAcwBYgDiDcBMFsGA1UdHwRU
        MFIwJ6AloCOGIWh0dHA6Ly9jaS50ZXN0LmdzbWEuY29tL0NSTC1BLmNybDAnoCWg
        I4YhaHR0cDovL2NpLnRlc3QuZ3NtYS5jb20vQ1JMLUIuY3JsMAoGCCqGSM49BAMC
        A0cAMEQCIAbGw59auXFdcsVl3PX/O/7z+3DvCuM6BzZmkTWG0O+SAiAWskTrRp8q
        L1hrwcMgtLZDG4902UH38YMrQyVSsWHfog==
        -----END CERTIFICATE-----
    """.trimIndent(),
    // Test CI
    // Specs: SGP.26 v3
    "34eecf13156518d48d30bdf06853404d115f955d" to """
        -----BEGIN CERTIFICATE-----
        MIIB4zCCAYqgAwIBAgIBADAKBggqhkjOPQQDAjBEMRAwDgYDVQQDDAdUZXN0IENJ
        MREwDwYDVQQLDAhURVNUQ0VSVDEQMA4GA1UECgwHUlNQVEVTVDELMAkGA1UEBhMC
        SVQwIBcNMjMwNTMxMTI1MDI4WhgPMjA1ODA1MzAxMjUwMjhaMEQxEDAOBgNVBAMM
        B1Rlc3QgQ0kxETAPBgNVBAsMCFRFU1RDRVJUMRAwDgYDVQQKDAdSU1BURVNUMQsw
        CQYDVQQGEwJJVDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABLlQW4kHaMJSrAK4
        nVKjGIgKWYxick+Y1x0MKO/Bsb3+KxMdnAObkPZjLosKlKCnH2bHUHhqRyDDSc2Y
        9+wB6A6jazBpMB0GA1UdDgQWBBQ07s8TFWUY1I0wvfBoU0BNEV+VXTAOBgNVHQ8B
        Af8EBAMCAQYwFwYDVR0gAQH/BA0wCzAJBgdngRIBAgEAMA8GA1UdEwEB/wQFMAMB
        Af8wDgYDVR0RBAcwBYgDiDcBMAoGCCqGSM49BAMCA0cAMEQCIEuYVB+bwdn5Z6sL
        eKFS07FnvHY03QqDm8XYxdjDAxZuAiBneNr+fBYeqDulQWfrGXFLDTbsFBENNdDj
        jvcHgHpATQ==
        -----END CERTIFICATE-----
    """.trimIndent(),
    "2209f61cd9ec5c9c854e787341ff83ecf9776a5b" to """
        -----BEGIN CERTIFICATE-----
        MIIB5DCCAYugAwIBAgIBADAKBggqhkjOPQQDAjBEMRAwDgYDVQQDDAdUZXN0IENJ
        MREwDwYDVQQLDAhURVNUQ0VSVDEQMA4GA1UECgwHUlNQVEVTVDELMAkGA1UEBhMC
        SVQwIBcNMjMwNjAyMTMwNTQzWhgPMjA1ODA2MDExMzA1NDNaMEQxEDAOBgNVBAMM
        B1Rlc3QgQ0kxETAPBgNVBAsMCFRFU1RDRVJUMRAwDgYDVQQKDAdSU1BURVNUMQsw
        CQYDVQQGEwJJVDBaMBQGByqGSM49AgEGCSskAwMCCAEBBwNCAASF7cCXanl/xSJe
        PwIeEUeZk4zPPM3iE16JbpOWPqPXaJwGmMKvHwQlRxiLtPWrRBalgkzrr4RgYIqD
        aTcnvxoFo2swaTAdBgNVHQ4EFgQUIgn2HNnsXJyFTnhzQf+D7Pl3alswDgYDVR0P
        AQH/BAQDAgEGMBcGA1UdIAEB/wQNMAswCQYHZ4ESAQIBADAPBgNVHRMBAf8EBTAD
        AQH/MA4GA1UdEQQHMAWIA4g3ATAKBggqhkjOPQQDAgNHADBEAiBLLHbhrIvy1Cue
        7lDUlQZY2EOK7/I/o2CQO0pj76OqzQIgTQ+kE02RPbMuflDbXKRuVDKFvfZ/vHEW
        QKvBPWehIXI=
        -----END CERTIFICATE-----
    """.trimIndent(),
    // Entrust eSIM Certification Authority (CA: Entrust)
    "16704b7f351e3607f18c4b70005c3a003dfd414a" to """
        -----BEGIN CERTIFICATE-----
        MIIC6DCCAo2gAwIBAgIRAIy4GT7M5nHsAAAAAFgsinowCgYIKoZIzj0EAwIwgbkx
        CzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1FbnRydXN0LCBJbmMuMSgwJgYDVQQLEx9T
        ZWUgd3d3LmVudHJ1c3QubmV0L2xlZ2FsLXRlcm1zMTkwNwYDVQQLEzAoYykgMjAx
        NiBFbnRydXN0LCBJbmMuIC0gZm9yIGF1dGhvcml6ZWQgdXNlIG9ubHkxLTArBgNV
        BAMTJEVudHJ1c3QgZVNJTSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAgFw0xNjEx
        MTYxNjA0MDJaGA8yMDUxMTAxNjE2MzQwMlowgbkxCzAJBgNVBAYTAlVTMRYwFAYD
        VQQKEw1FbnRydXN0LCBJbmMuMSgwJgYDVQQLEx9TZWUgd3d3LmVudHJ1c3QubmV0
        L2xlZ2FsLXRlcm1zMTkwNwYDVQQLEzAoYykgMjAxNiBFbnRydXN0LCBJbmMuIC0g
        Zm9yIGF1dGhvcml6ZWQgdXNlIG9ubHkxLTArBgNVBAMTJEVudHJ1c3QgZVNJTSBD
        ZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA
        BAdzwGHeQ1Wb2f4DmHTByR5/IWL3JugQ1U3908a++bHdlt+TTA7K4c5cYZ+51Yz/
        hg/bacxguPDh9uQUK6Wg3a6jcjBwMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/
        BAQDAgEGMBcGA1UdIAEB/wQNMAswCQYHZ4ESAQIBADAVBgNVHREEDjAMiApghkgB
        hvpsFAoAMB0GA1UdDgQWBBQWcEt/NR42B/GMS3AAXDoAPf1BSjAKBggqhkjOPQQD
        AgNJADBGAiEAspjXMvaBZyAg86Z0AAtT0yBRAi1EyaAfNz9kDJeAE04CIQC3efj8
        ATL7/tDBOhANy3cK8PS/1NIlu9vqMLCZsZvJ0Q==
        -----END CERTIFICATE-----
    """.trimIndent()
)