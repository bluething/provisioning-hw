package com.voxloud.provisioning.formatter;

import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class DeskConfigFormatterTest {
    private DeskConfigFormatter formatter;
    private ProvisioningProperties props;

    @BeforeEach
    void setUp() {
        formatter = new DeskConfigFormatter();
        props = new ProvisioningProperties();
        props.setDomain("voip.example.com");
        props.setPort(5060);
        props.setCodecs(Arrays.asList("ulaw", "alaw"));
    }

    @Test
    @DisplayName("supports() returns true for DESK, false otherwise")
    void supports_onlyDesk() {
        assertThat(formatter.supports(DeviceType.DESK)).isTrue();
        assertThat(formatter.supports(DeviceType.CONFERENCE)).isFalse();
    }

    @Test
    @DisplayName("format() without override produces default properties")
    void format_withoutOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("AA-BB-CC-DD-EE-FF")
                .model(DeviceType.DESK)
                .username("user1")
                .password("pass1")
                .build();

        String output = formatter.format(device, Optional.empty(), props);
        Properties p = new Properties();
        p.load(new StringReader(output));

        assertThat(p.getProperty("username")).isEqualTo("user1");
        assertThat(p.getProperty("password")).isEqualTo("pass1");
        assertThat(p.getProperty("domain")).isEqualTo("voip.example.com");
        assertThat(p.getProperty("port")).isEqualTo("5060");
        assertThat(p.getProperty("codecs")).isEqualTo("ulaw,alaw");
    }

    @Test
    @DisplayName("format() applies PROPERTIES override correctly")
    void format_withPropertiesOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("AA-BB-CC-DD-EE-FF")
                .model(DeviceType.DESK)
                .username("user1")
                .password("pass1")
                .build();

        String fragmentContent =
                "domain=override.example.com\n" +
                        "port=6000\n" +
                        "newKey=newValue";
        OverrideFragment override = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.PROPERTIES)
                .content(fragmentContent)
                .build();

        String output = formatter.format(device, Optional.of(override), props);
        Properties p = new Properties();
        p.load(new StringReader(output));

        // overridden props
        assertThat(p.getProperty("domain")).isEqualTo("override.example.com");
        assertThat(p.getProperty("port")).isEqualTo("6000");
        // new key added
        assertThat(p.getProperty("newKey")).isEqualTo("newValue");
        // unchanged keys still present
        assertThat(p.getProperty("username")).isEqualTo("user1");
        assertThat(p.getProperty("codecs")).isEqualTo("ulaw,alaw");
    }

    @Test
    @DisplayName("format() ignores JSON override fragment")
    void format_ignoresJsonOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("AA-BB-CC-DD-EE-FF")
                .model(DeviceType.DESK)
                .username("u")
                .password("p")
                .build();

        OverrideFragment jsonOverride = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.JSON)
                .content("{\"domain\":\"bad\"}")
                .build();

        String output = formatter.format(device, Optional.of(jsonOverride), props);
        Properties p = new Properties();
        p.load(new StringReader(output));

        // should stick to defaults
        assertThat(p.getProperty("domain")).isEqualTo("voip.example.com");
    }
}