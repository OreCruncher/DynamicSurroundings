/*
 * From Forge and tweaked not to reset the buffer as well as misc cleanup
 */

package org.orecruncher.dsurround.client.aurora;

import java.nio.ByteBuffer;
import java.util.List;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class VertexUploader
{
    public void draw(BufferBuilder bufferBuilderIn)
    {
        if (bufferBuilderIn.getVertexCount() > 0)
        {
            VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
            int i = vertexformat.getSize();
            ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            int size = list.size();
            for (int j = 0; j < size; ++j)
            {
                VertexFormatElement vertexformatelement = list.get(j);
                // moved to VertexFormatElement.preDraw
                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
            }

            GlStateManager.glDrawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());

            for (int i1 = 0; i1 < size; ++i1)
            {
                VertexFormatElement vertexformatelement1 = list.get(i1);
                // moved to VertexFormatElement.postDraw
                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }
    }
}