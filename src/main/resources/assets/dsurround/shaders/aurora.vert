//
// Created by Mattenii
// https://www.shadertoy.com/view/MsjfRG
// Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
//
// Shader was modified from the original to split it into separate
// vertex and frag shader scripts and adapted to fit the shader
// framework.
//
#version 120

void main() {
	gl_TexCoord[0] = gl_MultiTexCoord0;
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
