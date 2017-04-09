package estree;

import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import estree.Node.*;

public class FunctionExpression extends Node implements IFunctionExpression {
    IIdentifier id;
    List<IPattern> params;
    IBlockStatement body;

    public FunctionExpression(IIdentifier id, List<IPattern> params, IBlockStatement body) {
        type = FUNC_EXP;
        this.id = id;
        this.params = params;
        this.body = body;
    }

    @Override
    public IIdentifier getId() {
        return id;
    }

    @Override
    public List<IPattern> getParams() {
        return params;
    }

    @Override
    public IBlockStatement getBody() {
        return body;
    }

    @Override
    public JsonObject getEsTree() {
        JsonArrayBuilder paramsJb = Json.createArrayBuilder();
        for (IPattern param : params)
            paramsJb.add(param.getEsTree());
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add(KEY_TYPE, "FunctionExpression")
                .add(KEY_PARAMS, paramsJb);
        if (id != null) {
            jb.add(KEY_ID, id.getEsTree());
        } else {
            jb.addNull(KEY_ID);
        }
        if (body != null) {
            jb.add(KEY_BODY, body.getEsTree());
        } else {
            jb.addNull(KEY_BODY);
        }
        return jb.build();
    }

	@Override
	public void setBody(IBlockStatement body) {
		this.body = body;
	}

	@Override
	public Object accept(ESTreeBaseVisitor visitor) {
		return visitor.visitFunctionExpression(this);
	}
}
