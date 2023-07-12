#version 330 core

const float paletteWidth = 256;
const float paletteHeight = 256;

in vec2 fragUV;

out vec4 pixel;

uniform sampler2D paletteTexture;
uniform sampler2D imageTexture;
uniform int channels;

void main() {

	vec4 imageSample = texture(imageTexture , fragUV);
	float adjustedU = imageSample.r * paletteWidth;
	float adjustedV = imageSample.g * paletteHeight;
	
	vec4 paletteRead = texture(paletteTexture , vec2(adjustedU / paletteWidth , adjustedV / paletteHeight));

	if(channels <= 2) {

		float color = paletteRead.r;
		float alpha = channels == 2 ? paletteRead.g : 1.0f;
		pixel = vec4(color , color , color , alpha);

	} else pixel = paletteRead;

}