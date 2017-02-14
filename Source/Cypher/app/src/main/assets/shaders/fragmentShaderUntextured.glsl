precision mediump float;

uniform vec3 u_LightPos;

varying vec3 v_Position;
varying vec4 v_Color;

varying vec3 v_Normal;

void main()
{
    float distance = length(u_LightPos - v_Position);
    vec3 lightVector = normalize(u_LightPos - v_Position);
    float diffuse;
    if (gl_FrontFacing)
    {
        diffuse = max(dot(v_Normal, lightVector), 0.0);
    }
    else
    {
        diffuse = max(dot(-v_Normal, lightVector), 0.0);
    }
    diffuse = diffuse * (1.0 / (1.0 + (0.001 * distance)));
    diffuse = diffuse + 0.3;
    vec4 v_Base = vec4(0.0, 0.0, 0.0, 1.0);
    gl_FragColor = (v_Base + v_Color * diffuse);
}