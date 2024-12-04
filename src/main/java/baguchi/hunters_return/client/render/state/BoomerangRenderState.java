package baguchi.hunters_return.client.render.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class BoomerangRenderState extends EntityRenderState {
    public float xRot;
    public float yRot;
    public float speed;
    public boolean inGround;
    public ItemStackRenderState boomerang = new ItemStackRenderState();
}
