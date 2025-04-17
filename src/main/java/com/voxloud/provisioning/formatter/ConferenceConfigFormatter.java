package com.voxloud.provisioning.formatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ConferenceConfigFormatter implements ConfigFormatter {
    private final ObjectMapper mapper;

    @Override
    public boolean supports(DeviceType deviceType) {
        return deviceType == DeviceType.CONFERENCE;
    }

    @Override
    public String format(Device device,
                         Optional<OverrideFragment> override,
                         ProvisioningProperties props) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        root.put("username", device.getUsername());
        root.put("domain", props.getDomain());
        root.put("port", props.getPort());
        root.putPOJO("codecs", props.getCodecs());

        // apply JSON override if present
        if (override.isPresent() && override.get().getType() == FragmentType.JSON) {
            ObjectNode overrideNode = (ObjectNode) mapper.readTree(override.get().getContent());
            overrideNode.fieldNames()
                    .forEachRemaining(f -> root.set(f, overrideNode.get(f)));
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }
}
