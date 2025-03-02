package net.urosk.llms;

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
    public double cost;

}
