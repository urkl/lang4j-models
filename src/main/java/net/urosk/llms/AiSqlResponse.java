package net.urosk.llms;

import dev.langchain4j.model.output.structured.Description;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSqlResponse {

    public String sql;
    public String gptSummary;
     public String gptQuestion;
    public String title;
    public String prompt;

    @Description("Add price  of this api call in USD")
    public double cost;

}
