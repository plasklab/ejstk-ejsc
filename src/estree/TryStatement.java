package estree;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import estree.Node.*;

public class TryStatement extends Node implements ITryStatement {

    IBlockStatement block;
    ICatchClause handler;
    IBlockStatement finalizer;

    public TryStatement(IBlockStatement block, ICatchClause handler, IBlockStatement finalizer) {
        type = TRY_STMT;
        this.block = block;
        this.handler = handler;
        this.finalizer = finalizer;
    }

    @Override
    public JsonObject getEsTree() {
        JsonObjectBuilder jb = Json.createObjectBuilder()
                .add(KEY_TYPE, "TryStatement")
                .add(KEY_BLOCK, block.getEsTree());
        if (handler != null) {
            jb.add(KEY_HANDLER, handler.getEsTree());
        } else {
            jb.addNull(KEY_HANDLER);
        }
        if (finalizer != null) {
            jb.add(KEY_FINALIZER, finalizer.getEsTree());
        } else {
            jb.addNull(KEY_FINALIZER);
        }
        return jb.build();
    }

    @Override
    public IBlockStatement getBlock() {
        return block;
    }

    @Override
    public ICatchClause getHandler() {
        return handler;
    }

    @Override
    public IBlockStatement getFinalizer() {
        return finalizer;
    }

	@Override
	public void setBlock(IBlockStatement block) {
		this.block = block;
	}

	@Override
	public void setHandler(ICatchClause handler) {
		this.handler = handler;
	}

	@Override
	public void setFinalizer(IBlockStatement finalizer) {
		this.finalizer = finalizer;
	}

	@Override
	public Object accept(ESTreeBaseVisitor visitor) {
		return visitor.visitTryStatement(this);
	}

}
