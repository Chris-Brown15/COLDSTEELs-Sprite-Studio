//shader file for rendering a texture as is
#version 330 core

in vec2 fragUV;

out vec4 pixel;

uniform sampler2D sampler;
uniform int channels;

void main() {
	
	vec4 read = texture(sampler , fragUV);
	switch(channels) {

		case 1:
			pixel = vec4(read.rrr , 1.0);
			break;
		case 2:
			pixel = vec4(read.rrrg);
			break;
		case 3:
			pixel = vec4(read.rgb , 1.0);
			break;
		case 4:
			pixel = read;
			break;

	}

}