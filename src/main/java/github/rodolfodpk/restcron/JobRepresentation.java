package github.rodolfodpk.restcron;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

/**
 * A job schedule representation
 */
@Value
@ApiModel(description = "A job representation")
public class JobRepresentation {
    @ApiModelProperty(value="an identifier", required = true)
    String name;
    @ApiModelProperty(value="a message to be printed", required = true)
    String msg;
    @ApiModelProperty(value="a valid cron expression", required = true)
    String cron;
}
