package com.voxloud.provisioning.formatter;

import com.voxloud.provisioning.config.ProvisioningProperties;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceType;
import com.voxloud.provisioning.entity.FragmentType;
import com.voxloud.provisioning.entity.OverrideFragment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;

@Component
class DeskConfigFormatter implements ConfigFormatter {

    @Override
    public boolean supports(DeviceType deviceType) {
        return deviceType == DeviceType.DESK;
    }

    @Override
    public String format(Device device,
                         Optional<OverrideFragment> override,
                         ProvisioningProperties props) throws IOException {
        Properties p = new Properties();
        p.setProperty("username", device.getUsername());
        p.setProperty("password", device.getPassword());
        p.setProperty("domain", props.getDomain());
        p.setProperty("port", String.valueOf(props.getPort()));
        p.setProperty("codecs", String.join(",", props.getCodecs()));

        // apply overrides if any
        if (override.isPresent() && override.get().getType() == FragmentType.PROPERTIES) {
            Properties overrideProps = new Properties();
            overrideProps.load(new StringReader(override.get().getContent()));
            overrideProps.forEach((k, v) -> p.setProperty((String)k, (String)v));
        }

        StringWriter out = new StringWriter();
        p.store(out, null);
        return out.toString();
    }
}
