//shader file for rendering a texture as is
#version 330 core

in vec2 fragUV;

out vec4 pixel;

uniform sampler2D sampler;

void main() {
	
	pixel = texture(sampler , fragUV);

}