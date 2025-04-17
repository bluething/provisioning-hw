package com.voxloud.provisioning.formatter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConferenceConfigFormatterTest {
    private ConferenceConfigFormatter formatter;
    private ProvisioningProperties props;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        formatter = new ConferenceConfigFormatter(mapper);
        props = new ProvisioningProperties();
        props.setDomain("voip.example.com");
        props.setPort(5060);
        props.setCodecs(Arrays.asList("ulaw", "alaw"));
    }

    @Test
    @DisplayName("supports() returns true for CONFERENCE, false otherwise")
    void supports_onlyConference() {
        assertThat(formatter.supports(DeviceType.CONFERENCE)).isTrue();
        assertThat(formatter.supports(DeviceType.DESK)).isFalse();
    }

    @Test
    @DisplayName("format() without override produces default JSON")
    void format_withoutOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("11-22-33-44-55-66")
                .model(DeviceType.CONFERENCE)
                .username("confUser")
                .password("ignored")
                .build();

        String json = formatter.format(device, Optional.empty(), props);
        JsonNode root = mapper.readTree(json);

        assertThat(root.get("username").asText()).isEqualTo("confUser");
        assertThat(root.get("domain").asText()).isEqualTo("voip.example.com");
        assertThat(root.get("port").asInt()).isEqualTo(5060);
        assertThat(root.get("codecs")).isNotNull()
                .hasSize(2)
                .extracting(JsonNode::asText)
                .containsExactly("ulaw", "alaw");
    }

    @Test
    @DisplayName("format() merges JSON override correctly")
    void format_withJsonOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("11-22-33-44-55-66")
                .model(DeviceType.CONFERENCE)
                .username("confUser")
                .password("ignored")
                .build();

        String overrideContent = "{\n" +
                "\"domain\": \"override.example.com\",\n" +
                "\"timeout\": 30\n" +
                "}";
        OverrideFragment override = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.JSON)
                .content(overrideContent)
                .build();

        String json = formatter.format(device, Optional.of(override), props);
        JsonNode root = mapper.readTree(json);

        // overridden field
        assertThat(root.get("domain").asText()).isEqualTo("override.example.com");
        // new field
        assertThat(root.get("timeout").asInt()).isEqualTo(30);
        // unchanged defaults
        assertThat(root.get("username").asText()).isEqualTo("confUser");
        assertThat(root.get("port").asInt()).isEqualTo(5060);
    }

    @Test
    @DisplayName("format() ignores PROPERTIES override fragment")
    void format_ignoresPropertiesOverride() throws Exception {
        Device device = Device.builder()
                .macAddress("11-22-33-44-55-66")
                .model(DeviceType.CONFERENCE)
                .username("c")
                .password("p")
                .build();

        OverrideFragment propsOverride = OverrideFragment.builder()
                .device(device)
                .type(FragmentType.PROPERTIES)
                .content("domain=bad")
                .build();

        String json = formatter.format(device, Optional.of(propsOverride), props);
        JsonNode root = mapper.readTree(json);

        // should stick to defaults
        assertThat(root.get("domain").asText()).isEqualTo("voip.example.com");
    }

}