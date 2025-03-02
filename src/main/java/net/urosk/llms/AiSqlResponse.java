package net.urosk.llms;

import dev.langchain4j.model.output.structured.Description;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSqlResponse {

    public String sql;
    public String gptSummary;
    public String freemarker;
    public String gptQuestion;
    public String title;
    public String prompt;

    @Description("PRice of this api call in USD") // you can add an optional description to help an LLM have a better understanding

    public double cost;

}
