package fun.ogtimes.skywars.common.instance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstanceProperties {
    private String id;
    private String address;
    private int port;
}